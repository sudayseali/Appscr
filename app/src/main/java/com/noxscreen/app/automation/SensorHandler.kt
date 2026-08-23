package com.noxscreen.app.automation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import kotlin.math.abs
import kotlin.math.sqrt

class SensorHandler(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    var onProximityChanged: ((isNear: Boolean) -> Unit)? = null
    var onStationaryDetected: (() -> Unit)? = null
    var onMotionDetected: (() -> Unit)? = null
    var onShakeDetected: (() -> Unit)? = null

    private var isProximityActive = false
    private var isMotionActive = false
    private var isLightActive = false

    private var stationaryDurationMs = 10000L
    private val mainHandler = Handler(Looper.getMainLooper())
    private var stationaryCheckRunnable: Runnable? = null
    private var lastMovementTime = SystemClock.elapsedRealtime()

    private var lastAccelMagnitude = 9.8f
    private val movementThreshold = 0.8f
    private val shakeThreshold = 12.0f

    private var enableShake = false
    private var enableProximity = false

    // State for pocket mode
    private var isSensorProximityNear = false
    private var lux = 1000f
    private var hasLightSensor = false

    private val alpha = 0.8f
    private var gravityX = 0f
    private var gravityY = 0f
    private var gravityZ = 9.8f
    private var gravityInitialized = false
    private var hasGravity = false

    // State Machine
    private var pocketState = PocketState.OUT_OF_POCKET
    private var isCurrentlyInPocket = false
    private var stateTimerRunnable: Runnable? = null
    private var stateTransitionTime = 0L

    fun start(enableProximity: Boolean, enableMotion: Boolean, stationarySec: Int = 10, enableShake: Boolean = false) {
        stop()
        this.stationaryDurationMs = stationarySec * 1000L
        this.lastMovementTime = SystemClock.elapsedRealtime()
        this.enableShake = enableShake
        this.enableProximity = enableProximity

        val sm = sensorManager ?: return

        if (enableProximity) {
            if (proximitySensor != null && !isProximityActive) {
                isProximityActive = sm.registerListener(
                    this,
                    proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
            if (lightSensor != null && !isLightActive) {
                isLightActive = sm.registerListener(
                    this,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
                hasLightSensor = isLightActive
            }
        }

        if ((enableProximity || enableMotion || enableShake) && accelerometer != null && !isMotionActive) {
            val delay = if (enableShake) SensorManager.SENSOR_DELAY_UI else SensorManager.SENSOR_DELAY_NORMAL
            val registered = sm.registerListener(
                this,
                accelerometer,
                delay
            )
            isMotionActive = registered
            hasGravity = registered

            if (enableMotion && registered) {
                scheduleStationaryCheck()
            }
        }
    }

    fun stop() {
        sensorManager?.let { sm ->
            if (isProximityActive || isMotionActive || isLightActive) {
                try {
                    sm.unregisterListener(this)
                } catch (e: Exception) {
                    // Safe unregister
                }
            }
        }
        isProximityActive = false
        isMotionActive = false
        isLightActive = false
        hasLightSensor = false
        hasGravity = false
        gravityInitialized = false

        stationaryCheckRunnable?.let { mainHandler.removeCallbacks(it) }
        stationaryCheckRunnable = null
        cancelStateTimer()

        pocketState = PocketState.OUT_OF_POCKET
        isCurrentlyInPocket = false
    }

    private fun scheduleStationaryCheck() {
        stationaryCheckRunnable?.let { mainHandler.removeCallbacks(it) }
        stationaryCheckRunnable = Runnable {
            if (isMotionActive) {
                val elapsedTime = SystemClock.elapsedRealtime() - lastMovementTime
                if (elapsedTime >= stationaryDurationMs) {
                    onStationaryDetected?.invoke()
                } else {
                    mainHandler.postDelayed(stationaryCheckRunnable!!, 1000L)
                }
            }
        }
        mainHandler.postDelayed(stationaryCheckRunnable!!, 1000L)
    }

    private fun cancelStateTimer() {
        stateTimerRunnable?.let { mainHandler.removeCallbacks(it) }
        stateTimerRunnable = null
    }

    private fun transitionState(newState: PocketState, delayMs: Long = 0) {
        if (pocketState == newState && delayMs == 0L) return
        pocketState = newState
        cancelStateTimer()

        if (delayMs > 0) {
            stateTransitionTime = SystemClock.elapsedRealtime() + delayMs
            stateTimerRunnable = Runnable { processPocketState() }
            mainHandler.postDelayed(stateTimerRunnable!!, delayMs)
        } else {
            if (newState == PocketState.IN_POCKET && !isCurrentlyInPocket) {
                isCurrentlyInPocket = true
                onProximityChanged?.invoke(true)
            } else if (newState == PocketState.OUT_OF_POCKET && isCurrentlyInPocket) {
                isCurrentlyInPocket = false
                onProximityChanged?.invoke(false)
            }
        }
    }

    private fun processPocketState() {
        if (!enableProximity) return

        val confidence = PocketDecisionEngine.calculateConfidence(
            isProximityNear = isSensorProximityNear,
            hasLightSensor = hasLightSensor,
            lux = lux,
            hasGravity = hasGravity,
            gx = gravityX,
            gy = gravityY,
            gz = gravityZ
        )

        when (pocketState) {
            PocketState.OUT_OF_POCKET -> {
                if (PocketDecisionEngine.shouldEnterPocket(confidence)) {
                    transitionState(PocketState.DETECTING_POCKET, PocketConstants.POCKET_ENTER_DEBOUNCE_MS)
                }
            }
            PocketState.DETECTING_POCKET -> {
                if (!PocketDecisionEngine.shouldEnterPocket(confidence)) {
                    transitionState(PocketState.OUT_OF_POCKET)
                } else if (SystemClock.elapsedRealtime() >= stateTransitionTime) {
                    transitionState(PocketState.IN_POCKET)
                }
            }
            PocketState.IN_POCKET -> {
                if (PocketDecisionEngine.shouldExitPocket(confidence)) {
                    transitionState(PocketState.DETECTING_REMOVAL, PocketConstants.POCKET_EXIT_DEBOUNCE_MS)
                }
            }
            PocketState.DETECTING_REMOVAL -> {
                if (!PocketDecisionEngine.shouldExitPocket(confidence)) {
                    transitionState(PocketState.IN_POCKET)
                } else if (SystemClock.elapsedRealtime() >= stateTransitionTime) {
                    transitionState(PocketState.OUT_OF_POCKET)
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_PROXIMITY -> {
                val distance = event.values[0]
                val maxRange = event.sensor.maximumRange
                isSensorProximityNear = distance < 0.5f || (distance < maxRange && distance < 4.0f)
                processPocketState()
            }
            Sensor.TYPE_LIGHT -> {
                lux = event.values[0]
                processPocketState()
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                if (!gravityInitialized) {
                    gravityX = x
                    gravityY = y
                    gravityZ = z
                    gravityInitialized = true
                } else {
                    gravityX = alpha * gravityX + (1 - alpha) * x
                    gravityY = alpha * gravityY + (1 - alpha) * y
                    gravityZ = alpha * gravityZ + (1 - alpha) * z
                }

                processPocketState()

                val sqMagnitude = x * x + y * y + z * z
                val magnitude = sqrt(sqMagnitude.toDouble()).toFloat()
                val delta = abs(magnitude - lastAccelMagnitude)
                lastAccelMagnitude = magnitude

                if (delta > movementThreshold) {
                    lastMovementTime = SystemClock.elapsedRealtime()
                    onMotionDetected?.invoke()
                }

                if (enableShake && delta > shakeThreshold) {
                    onShakeDetected?.invoke()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
