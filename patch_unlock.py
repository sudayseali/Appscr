import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target = """                setOnClickListener {
                    handler.removeCallbacks(resetToBlackRunnable)
                    val config = smartAutomationManager.settings.getConfig()
                    if (config.isBiometricEnabled) {
                        val intent = Intent(this@BlackScreenService, BiometricAuthActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                    } else {
                        smartAutomationManager.handleManualDismiss()
                        showFloatingBubbleInternal()
                    }
                }"""

replacement = """                setOnClickListener {
                    handler.removeCallbacks(resetToBlackRunnable)
                    val config = smartAutomationManager.settings.getConfig()
                    if (config.isBiometricEnabled) {
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
                    }
                }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
        f.write(content)
    print("Replaced Unlock Click!")
else:
    print("Unlock Click not found")
