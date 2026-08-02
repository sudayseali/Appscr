import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target1 = """    private lateinit var smartAutomationManager: com.noxscreen.app.automation.SmartAutomationManager"""
replacement1 = """    private lateinit var smartAutomationManager: com.noxscreen.app.automation.SmartAutomationManager
    private lateinit var usageLimitMonitor: com.noxscreen.app.automation.UsageLimitMonitor"""
content = content.replace(target1, replacement1)

target2 = """        smartAutomationManager = com.noxscreen.app.automation.SmartAutomationManager(
            context = this,
            onTriggerOverlay = { bySensor -> 
                if (bySensor) {
                    showBlackoutInternal()
                } else {
                    showBlackoutInternal()
                }
            },
            onRemoveOverlay = {
                showFloatingBubbleInternal()
            }
        )"""
replacement2 = """        smartAutomationManager = com.noxscreen.app.automation.SmartAutomationManager(
            context = this,
            onTriggerOverlay = { bySensor -> 
                if (bySensor) {
                    showBlackoutInternal()
                } else {
                    showBlackoutInternal()
                }
            },
            onRemoveOverlay = {
                showFloatingBubbleInternal()
            }
        )
        
        usageLimitMonitor = com.noxscreen.app.automation.UsageLimitMonitor(
            context = this,
            automationSettings = smartAutomationManager.settings,
            onTriggerBlock = {
                showBlackoutInternal()
                
                android.widget.Toast.makeText(
                    this,
                    "Waqtigaagii wuu dhamaaday (Usage Limit Reached)",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        )"""
content = content.replace(target2, replacement2)

target3 = """    override fun onDestroy() {
        super.onDestroy()"""
replacement3 = """    override fun onDestroy() {
        super.onDestroy()
        usageLimitMonitor.stopMonitoring()"""
content = content.replace(target3, replacement3)

target4 = """        if (intent?.action == "START_BLACKOUT") {
            smartAutomationManager.handleUserActivation()
            smartAutomationManager.startSensors()
        } else {
            showFloatingBubbleInternal()
            smartAutomationManager.startSensors()
        }
        return START_STICKY"""
replacement4 = """        if (intent?.action == "START_BLACKOUT") {
            smartAutomationManager.handleUserActivation()
            smartAutomationManager.startSensors()
        } else {
            showFloatingBubbleInternal()
            smartAutomationManager.startSensors()
        }
        usageLimitMonitor.startMonitoring()
        return START_STICKY"""
content = content.replace(target4, replacement4)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
