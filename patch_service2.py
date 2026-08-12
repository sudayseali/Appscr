import re

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

helper_method = """
    private fun handleUnlockRequest() {
        val config = smartAutomationManager.settings.getConfig()
        handler.removeCallbacks(resetToBlackRunnable)
        if (config.isBiometricEnabled) {
            blackoutView?.visibility = android.view.View.GONE
            com.noxscreen.app.security.AuthenticationManager.startAuthentication(
                onSuccess = {
                    smartAutomationManager.handleManualDismiss()
                    showFloatingBubbleInternal()
                },
                onFailure = {
                    blackoutView?.visibility = android.view.View.VISIBLE
                    isUnlockScreenVisible = false
                    aodContainer?.visibility = android.view.View.GONE
                    unlockButton?.visibility = android.view.View.GONE
                    handler.removeCallbacks(resetToBlackRunnable)
                    handler.postDelayed(resetToBlackRunnable, 10000)
                }
            )
            val intent = android.content.Intent(this@BlackScreenService, BiometricAuthActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        } else {
            smartAutomationManager.handleManualDismiss()
            showFloatingBubbleInternal()
        }
    }

    @android.annotation.SuppressLint("ClickableViewAccessibility")
    private fun setupBlackoutView() {"""

content = content.replace('    @SuppressLint("ClickableViewAccessibility")\n    private fun setupBlackoutView() {', helper_method)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
