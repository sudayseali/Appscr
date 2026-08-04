import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target = """                    if (config.isBiometricEnabled) {
                        val intent = Intent(this@BlackScreenService, BiometricAuthActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                    } else {
                        // Launch AdActivity for 3 ads before unlocking
                        val intent = Intent(this@BlackScreenService, AdLauncherActivity::class.java).apply {
                            putExtra("ADS_COUNT", 3)
                            putExtra("ON_COMPLETE_ACTION", "UNLOCK_SCREEN")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        startActivity(intent)
                    }"""

replacement = """                    if (config.isBiometricEnabled) {
                        val intent = Intent(this@BlackScreenService, BiometricAuthActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                    } else {
                        smartAutomationManager.handleManualDismiss()
                        showFloatingBubbleInternal()
                    }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
        f.write(content)
    print("Reverted unlock ad!")
else:
    print("Unlock ad target not found")

target2 = """                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        // Launch AdActivity for 3 ads before activating
                        val intent = android.content.Intent(this@BlackScreenService, AdLauncherActivity::class.java).apply {
                            putExtra("ADS_COUNT", 3)
                            putExtra("ON_COMPLETE_ACTION", "START_BLACK_SCREEN")
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                    }
                    true
                }"""
replacement2 = """                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        smartAutomationManager.handleUserActivation()
                    }
                    true
                }"""
                
if target2 in content:
    content = content.replace(target2, replacement2)
    with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
        f.write(content)
    print("Reverted service click ad!")
else:
    print("Service click target not found")
