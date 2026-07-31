import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target = """                setOnClickListener {
                    handler.removeCallbacks(resetToBlackRunnable)
                    smartAutomationManager.handleManualDismiss()
                    showFloatingBubbleInternal()
                }"""

replacement = """                setOnClickListener {
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

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
