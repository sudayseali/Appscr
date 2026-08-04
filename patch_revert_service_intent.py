import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target = """        if (intent?.action == "BIOMETRIC_SUCCESS" || intent?.action == "UNLOCK_SCREEN") {
            smartAutomationManager.handleManualDismiss()
            showFloatingBubbleInternal()
            return START_STICKY
        }"""
replacement = """        if (intent?.action == "BIOMETRIC_SUCCESS") {
            smartAutomationManager.handleManualDismiss()
            showFloatingBubbleInternal()
            return START_STICKY
        }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
        f.write(content)
    print("Reverted service intent!")
else:
    print("Service intent target not found")
