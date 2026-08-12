import re

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

# Add the handleUnlockRequest method before setupUnlockScreen
helper_method = """
    private fun handleUnlockRequest() {
        val config = smartAutomationManager.settings.getConfig()
        handler.removeCallbacks(resetToBlackRunnable)
        if (config.isBiometricEnabled) {
            blackoutView?.visibility = View.GONE
            com.noxscreen.app.security.AuthenticationManager.startAuthentication(
                onSuccess = {
                    smartAutomationManager.handleManualDismiss()
                    showFloatingBubbleInternal()
                },
                onFailure = {
                    blackoutView?.visibility = View.VISIBLE
                    isUnlockScreenVisible = false
                    aodContainer?.visibility = View.GONE
                    unlockButton?.visibility = View.GONE
                    handler.removeCallbacks(resetToBlackRunnable)
                    handler.postDelayed(resetToBlackRunnable, 10000)
                }
            )
            val intent = Intent(this@BlackScreenService, BiometricAuthActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        } else {
            smartAutomationManager.handleManualDismiss()
            showFloatingBubbleInternal()
        }
    }

    @Suppress("DEPRECATION")
    private fun setupUnlockScreen() {"""

content = content.replace('    @Suppress("DEPRECATION")\n    private fun setupUnlockScreen() {', helper_method)

# Replace the specific blocks with handleUnlockRequest()
pattern = r"""                                        handler\.removeCallbacks\(resetToBlackRunnable\)\s*if \(config\.isBiometricEnabled\) \{\s*blackoutView\?\.visibility = View\.GONE\s*val intent = Intent\(this@BlackScreenService, BiometricAuthActivity::class\.java\)\s*intent\.addFlags\(Intent\.FLAG_ACTIVITY_NEW_TASK or Intent\.FLAG_ACTIVITY_CLEAR_TOP\)\s*startActivity\(intent\)\s*\} else \{\s*smartAutomationManager\.handleManualDismiss\(\)\s*showFloatingBubbleInternal\(\)\s*\}"""

content = re.sub(pattern, "                                        handleUnlockRequest()", content)

pattern2 = r"""                            handler\.removeCallbacks\(resetToBlackRunnable\)\s*if \(config\.isBiometricEnabled\) \{\s*blackoutView\?\.visibility = View\.GONE\s*val intent = Intent\(this@BlackScreenService, BiometricAuthActivity::class\.java\)\s*intent\.addFlags\(Intent\.FLAG_ACTIVITY_NEW_TASK or Intent\.FLAG_ACTIVITY_CLEAR_TOP\)\s*startActivity\(intent\)\s*\} else \{\s*smartAutomationManager\.handleManualDismiss\(\)\s*showFloatingBubbleInternal\(\)\s*\}"""

content = re.sub(pattern2, "                            handleUnlockRequest()", content)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
