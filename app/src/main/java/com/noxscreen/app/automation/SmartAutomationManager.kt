package com.noxscreen.app.automation

import android.content.Context

class SmartAutomationManager(
    private val context: Context,
    private val onTriggerOverlay: (triggeredBySensor: Boolean) -> Unit,
    private val onRemoveOverlay: () -> Unit
) {
    val settings = AutomationSettings(context)
    val timerHandler = TimerHandler()
    val sensorHandler = SensorHandler(context)
    val analyticsManager = UsageAnalyticsManager(context)

    private var triggeredBySensor = false

    init {
        setupSensorCallbacks()
    }

    private fun setupSensorCallbacks() {
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
        triggeredBySensor = false
    }

    fun startSensors() {
        val config = settings.getConfig()
        if (config.isPocketModeEnabled || config.isMotionDetectionEnabled) {
            sensorHandler.start(
                enableProximity = config.isPocketModeEnabled,
                enableMotion = config.isMotionDetectionEnabled,
                stationarySec = config.stationaryDurationSeconds
            )
        }
    }

    fun stopSensors() {
        sensorHandler.stop()
        timerHandler.cancelTimer()
    }
}
