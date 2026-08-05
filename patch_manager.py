with open("app/src/main/java/com/noxscreen/app/automation/SmartAutomationManager.kt", "r", encoding="utf-8") as f:
    content = f.read()

sensor_callbacks = """        sensorHandler.onFaceDownDetected = {
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
        }"""
        
content = content.replace("    private fun setupSensorCallbacks() {", "    private fun setupSensorCallbacks() {\n" + sensor_callbacks)

start_sensors_target = """    fun startSensors() {
        val config = settings.getConfig()
        if (config.isPocketModeEnabled || config.isMotionDetectionEnabled) {
            sensorHandler.start(
                enableProximity = config.isPocketModeEnabled,
                enableMotion = config.isMotionDetectionEnabled,
                stationarySec = config.stationaryDurationSeconds
            )
        }
    }"""
    
start_sensors_replacement = """    fun startSensors() {
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
    }"""
    
content = content.replace(start_sensors_target, start_sensors_replacement)

with open("app/src/main/java/com/noxscreen/app/automation/SmartAutomationManager.kt", "w", encoding="utf-8") as f:
    f.write(content)
