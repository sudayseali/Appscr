package com.noxscreen.app.automation

import android.content.Context

class SmartAutomationManager(
    private val context: Context,
    private val onTriggerOverlay: (triggeredBySensor: Boolean) -> Unit,
    private val onRemoveOverlay: () -> Unit,
    var onSleepTimerTick: ((remainingSec: Long) -> Unit)? = null,
    var onSleepTimerExpired: (() -> Unit)? = null
) {
    val settings = AutomationSettings(context)
    val timerHandler = TimerHandler()
    val sleepTimerHandler = SleepTimerHandler()
    val sensorHandler = SensorHandler(context)
    val analyticsManager = UsageAnalyticsManager(context)

    private var triggeredBySensor = false

    init {
        setupSensorCallbacks()
    }

    private fun setupSensorCallbacks() {
        sensorHandler.onFaceDownDetected = {
            val config = settings.getConfig()
            if (config.isFlipToSleepEnabled) {
                if (timerHandler.isTimerRunning) {
                    timerHandler.cancelTimer()
                }
                triggeredBySensor = true
                onTriggerOverlay(true)
            }
        }

        sensorHandler.onShakeDetected = {
            val config = settings.getConfig()
            if (config.isShakeToWakeEnabled) {
                // If the screen is black, wake it up
                if (triggeredBySensor) {
                    triggeredBySensor = false
                    stopSleepTimer()
                    onRemoveOverlay()
                } else {
                    // Always try to remove overlay on shake if possible
                    triggeredBySensor = false
                    stopSleepTimer()
                    onRemoveOverlay()
                }
            }
        }
        sensorHandler.onProximityChanged = { isNear ->
            val config = settings.getConfig()
            if (config.isPocketModeEnabled) {
                if (isNear) {
                    // Sensors override timer: cancel any pending timer and trigger overlay immediately
                    if (timerHandler.isTimerRunning) {
                        timerHandler.cancelTimer()
                    }
                    triggeredBySensor = true
                    onTriggerOverlay(true)
                } else {
                    // Remove overlay ONLY if triggered by proximity sensor
                    if (triggeredBySensor) {
                        triggeredBySensor = false
                        stopSleepTimer()
                        onRemoveOverlay()
                    }
                }
            }
        }

        sensorHandler.onStationaryDetected = {
            val config = settings.getConfig()
            if (config.isMotionDetectionEnabled) {
                if (timerHandler.isTimerRunning) {
                    timerHandler.cancelTimer()
                }
                triggeredBySensor = true
                onTriggerOverlay(true)
            }
        }

        sensorHandler.onMotionDetected = {
            // Movement detected - reset stationary timer if needed
        }
    }

    fun startSleepTimerIfEnabled() {
        val config = settings.getConfig()
        if (config.isSleepTimerEnabled && config.sleepTimerDurationMinutes > 0) {
            sleepTimerHandler.startSleepTimer(
                durationMinutes = config.sleepTimerDurationMinutes,
                onTick = { remainingSec ->
                    onSleepTimerTick?.invoke(remainingSec)
                },
                onFinished = {
                    onSleepTimerExpired?.invoke()
                }
            )
        }
    }

    fun stopSleepTimer() {
        sleepTimerHandler.stopSleepTimer()
    }

    fun handleUserActivation() {
        analyticsManager.recordActivation()
        val config = settings.getConfig()

        if (config.isTimerEnabled && config.timerDurationSeconds > 0) {
            timerHandler.startTimer(config.timerDurationSeconds) {
                triggeredBySensor = false
                onTriggerOverlay(false)
            }
        } else {
            triggeredBySensor = false
            onTriggerOverlay(false)
        }
    }

    fun handleManualDismiss() {
        timerHandler.cancelTimer()
        stopSleepTimer()
        triggeredBySensor = false
    }

    fun startSensors() {
        val config = settings.getConfig()
        if (config.isPocketModeEnabled || config.isMotionDetectionEnabled || config.isFlipToSleepEnabled || config.isShakeToWakeEnabled) {
            sensorHandler.start(
                enableProximity = config.isPocketModeEnabled,
                enableMotion = config.isMotionDetectionEnabled,
                stationarySec = config.stationaryDurationSeconds,
                enableFaceDown = config.isFlipToSleepEnabled,
                enableShake = config.isShakeToWakeEnabled
            )
        }
    }

    fun stopSensors() {
        sensorHandler.stop()
        timerHandler.cancelTimer()
        stopSleepTimer()
    }
}
