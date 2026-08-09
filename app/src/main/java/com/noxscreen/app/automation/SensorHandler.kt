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
    var onFaceDownDetected: (() -> Unit)? = null
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

    private var enableFaceDown = false
    private var enableShake = false
    private var isFaceDownTriggered = false
    private var faceDownStartTime = 0L

    private var proximityRunnable: Runnable? = null
    private var isProximityTriggered = false

    // State for pocket mode
    private var isSensorProximityNear = false
    private var isDark = false

    fun start(enableProximity: Boolean, enableMotion: Boolean, stationarySec: Int = 10, enableFaceDown: Boolean = false, enableShake: Boolean = false) {
        stop()
        this.stationaryDurationMs = stationarySec * 1000L
        this.lastMovementTime = System.currentTimeMillis()
        this.enableFaceDown = enableFaceDown
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

        if ((enableMotion || enableFaceDown || enableShake) && accelerometer != null) {
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
        // Pocket mode is triggered if proximity is near OR (it's completely dark AND we assume it's in a pocket)
        // Soft fabrics might not trigger proximity correctly, but they will block light.
        // We only use light sensor as a fallback if proximity is not near, but it has to be VERY dark (0 lux)
        val isNear = isSensorProximityNear || isDark

        if (isNear) {
            if (!isProximityTriggered) {
                proximityRunnable?.let { mainHandler.removeCallbacks(it) }
                proximityRunnable = Runnable {
                    isProximityTriggered = true
                    onProximityChanged?.invoke(true)
                }
                mainHandler.postDelayed(proximityRunnable!!, 500L) // Delay to prevent false positives
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
                // Robust proximity check: some sensors return maxRange for far, others return 5.0 for far.
                isSensorProximityNear = distance < maxRange && distance < 5.0f
                checkPocketMode()
            }
            Sensor.TYPE_LIGHT -> {
                val lux = event.values[0]
                // Very dark means in pocket or face down on a surface. 
                // We use < 2.0 lux as a threshold for "in pocket" or covered by fabric.
                isDark = lux < 2.0f
                checkPocketMode()
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
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

                if (enableFaceDown) {
                    // Face down: screen pointing to the ground (z is negative gravity)
                    // Added a wider margin for x and y to make it trigger more reliably
                    // when placed on slightly uneven surfaces or just held somewhat flat.
                    val isFaceDownNow = z < -6.0f && abs(x) < 5.0f && abs(y) < 5.0f
                    if (isFaceDownNow) {
                        if (!isFaceDownTriggered) {
                            if (faceDownStartTime == 0L) {
                                faceDownStartTime = System.currentTimeMillis()
                            } else if (System.currentTimeMillis() - faceDownStartTime > 300L) {
                                onFaceDownDetected?.invoke()
                                isFaceDownTriggered = true
                            }
                        }
                    } else {
                        // Allow small noise without immediately resetting
                        if (z > -2.0f || abs(x) > 7.0f || abs(y) > 7.0f) {
                            faceDownStartTime = 0L
                            isFaceDownTriggered = false
                        }
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
