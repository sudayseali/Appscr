import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target = """    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            stopSelf()
            return START_NOT_STICKY
        }
        
        if (intent?.action == "START_BLACKOUT") {
            smartAutomationManager.handleUserActivation()
            smartAutomationManager.startSensors()
        } else {
            showFloatingBubbleInternal()
            smartAutomationManager.startSensors()
        }
        return START_STICKY
    }"""

replacement = """    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            stopSelf()
            return START_NOT_STICKY
        }
        
        if (intent?.action == "BIOMETRIC_SUCCESS") {
            smartAutomationManager.handleManualDismiss()
            showFloatingBubbleInternal()
            return START_STICKY
        }
        
        if (intent?.action == "START_BLACKOUT") {
            smartAutomationManager.handleUserActivation()
            smartAutomationManager.startSensors()
        } else {
            showFloatingBubbleInternal()
            smartAutomationManager.startSensors()
        }
        return START_STICKY
    }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
