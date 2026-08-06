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

    var onProximityChanged: ((isNear: Boolean) -> Unit)? = null
    var onStationaryDetected: (() -> Unit)? = null
    var onMotionDetected: (() -> Unit)? = null
    var onFaceDownDetected: (() -> Unit)? = null
    var onShakeDetected: (() -> Unit)? = null

    private var isProximityActive = false
    private var isMotionActive = false

    private var stationaryDurationMs = 10000L
    private val mainHandler = Handler(Looper.getMainLooper())
    private var stationaryCheckRunnable: Runnable? = null
    private var lastMovementTime = System.currentTimeMillis()

    private var lastAccelMagnitude = 9.8f
    private val movementThreshold = 0.8f // m/s² change threshold
    private val shakeThreshold = 12.0f // m/s² change threshold for shake
    private var enableFaceDown = false
    private var enableShake = false
    private var isFaceDownTriggered = false
    private var faceDownCheckRunnable: Runnable? = null

    fun start(enableProximity: Boolean, enableMotion: Boolean, stationarySec: Int = 10, enableFaceDown: Boolean = false, enableShake: Boolean = false) {
        stop()
        this.stationaryDurationMs = stationarySec * 1000L
        this.lastMovementTime = System.currentTimeMillis()
        this.enableFaceDown = enableFaceDown
        this.enableShake = enableShake

        if (sensorManager == null) return

        if (enableProximity && proximitySensor != null) {
            isProximityActive = sensorManager.registerListener(
                this,
                proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        if ((enableMotion || enableFaceDown || enableShake) && accelerometer != null) {
            isMotionActive = sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            scheduleStationaryCheck()
        }
    }

    fun stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this)
        }
        isProximityActive = false
        isMotionActive = false
        stationaryCheckRunnable?.let { mainHandler.removeCallbacks(it) }
        stationaryCheckRunnable = null
        faceDownCheckRunnable?.let { mainHandler.removeCallbacks(it) }
        faceDownCheckRunnable = null
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

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        when (event.sensor.type) {
            Sensor.TYPE_PROXIMITY -> {
                val distance = event.values[0]
                val maxRange = event.sensor.maximumRange
                val isNear = distance < maxRange
                onProximityChanged?.invoke(isNear)
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
                    val isFaceDownNow = z < -7.0f && abs(x) < 5.0f && abs(y) < 5.0f
                    if (isFaceDownNow) {
                        if (!isFaceDownTriggered && faceDownCheckRunnable == null) {
                            faceDownCheckRunnable = Runnable {
                                onFaceDownDetected?.invoke()
                                isFaceDownTriggered = true
                                faceDownCheckRunnable = null
                            }
                            mainHandler.postDelayed(faceDownCheckRunnable!!, 500L)
                        }
                    } else {
                        faceDownCheckRunnable?.let { mainHandler.removeCallbacks(it) }
                        faceDownCheckRunnable = null
                        isFaceDownTriggered = false
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
