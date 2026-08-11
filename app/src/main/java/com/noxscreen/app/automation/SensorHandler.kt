package com.noxscreen.app.automation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
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
    private var lastMovementTime = System.currentTimeMillis()

    private var lastAccelMagnitude = 9.8f
    private val movementThreshold = 0.8f
    private val shakeThreshold = 12.0f

    private var enableShake = false

    private var proximityRunnable: Runnable? = null
    private var isProximityTriggered = false

    // State for pocket mode
    private var isSensorProximityNear = false
    private var isDark = false

    private var accelX = 0f
    private var accelY = 0f
    private var accelZ = 9.8f

    fun start(enableProximity: Boolean, enableMotion: Boolean, stationarySec: Int = 10, enableShake: Boolean = false) {

        stop()
        this.stationaryDurationMs = stationarySec * 1000L
        this.lastMovementTime = System.currentTimeMillis()
        this.enableShake = enableShake

        if (sensorManager == null) return

        if (enableProximity) {
            if (proximitySensor != null) {
                isProximityActive = sensorManager.registerListener(
                    this,
                    proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
            if (lightSensor != null) {
                isLightActive = sensorManager.registerListener(
                    this,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
        }

        if ((enableProximity || enableMotion || enableShake) && accelerometer != null) {
            isMotionActive = sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI // Faster for reliable flip and shake
            )
            if (enableMotion) {
                scheduleStationaryCheck()
            }
        }
    }

    fun stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this)
        }
        isProximityActive = false
        isMotionActive = false
        isLightActive = false
        stationaryCheckRunnable?.let { mainHandler.removeCallbacks(it) }
        stationaryCheckRunnable = null
        proximityRunnable?.let { mainHandler.removeCallbacks(it) }
    }

    private fun scheduleStationaryCheck() {
        stationaryCheckRunnable?.let { mainHandler.removeCallbacks(it) }
        stationaryCheckRunnable = Runnable {
            if (isMotionActive) {
                val elapsedTime = System.currentTimeMillis() - lastMovementTime
                if (elapsedTime >= stationaryDurationMs) {
                    onStationaryDetected?.invoke()
                } else {
                    mainHandler.postDelayed(stationaryCheckRunnable!!, 1000L)
                }
            }
        }
        mainHandler.postDelayed(stationaryCheckRunnable!!, 1000L)
    }

    private fun checkPocketMode() {
        // High Precision Pocket Mode Algorithm for Android 8+:
        // 1. Proximity is NEAR
        // 2. Not lying flat face-up under normal light (prevents accidental triggers when waving hand over desk)
        // 3. Or Light sensor detects dark environment (< 5 lux) combined with proximity near
        val isFlatFaceUp = accelZ > 7.5f && abs(accelX) < 3.5f && abs(accelY) < 3.5f
        val inPocket = isSensorProximityNear && (isDark || !isFlatFaceUp)

        if (inPocket) {
            if (!isProximityTriggered) {
                proximityRunnable?.let { mainHandler.removeCallbacks(it) }
                proximityRunnable = Runnable {
                    isProximityTriggered = true
                    onProximityChanged?.invoke(true)
                }
                mainHandler.postDelayed(proximityRunnable!!, 350L) // 350ms debounce for responsive pocket detection
            }
        } else {
            proximityRunnable?.let { mainHandler.removeCallbacks(it) }
            if (isProximityTriggered) {
                isProximityTriggered = false
                onProximityChanged?.invoke(false)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_PROXIMITY -> {
                val distance = event.values[0]
                val maxRange = event.sensor.maximumRange
                // Robust proximity check for all Android hardware (binary vs continuous proximity sensors)
                isSensorProximityNear = distance < 0.5f || (distance < maxRange && distance < 4.0f)
                checkPocketMode()
            }
            Sensor.TYPE_LIGHT -> {
                val lux = event.values[0]
                // Pocket or covered environment threshold
                isDark = lux < 5.0f
                checkPocketMode()
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                
                accelX = x
                accelY = y
                accelZ = z
                
                // Only trigger checkPocketMode if pocket mode relies on it
                checkPocketMode()
                
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val delta = abs(magnitude - lastAccelMagnitude)
                lastAccelMagnitude = magnitude

                if (delta > movementThreshold) {
                    lastMovementTime = System.currentTimeMillis()
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
