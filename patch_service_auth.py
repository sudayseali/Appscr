import re

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

# Fix 1: onStartCommand STOP_SERVICE
pattern_stop = r"if \(intent\?\.action == \"STOP_SERVICE\"\) \{.*?isRunning = true"
replacement_stop = """if (intent?.action == "AUTH_SUCCESS_STOP") {
            stopSelfAndCleanUp()
            return START_NOT_STICKY
        }
        if (intent?.action == "STOP_SERVICE") {
            if (usageLimitMonitor.isCurrentlyBlocked) {
                // Ignore STOP_SERVICE if currently blocked by focus mode
                return START_STICKY
            }
            val config = smartAutomationManager.settings.getConfig()
            if (config.isBiometricEnabled) {
                com.noxscreen.app.security.AuthenticationManager.setAuthenticating()
                val successIntent = android.app.PendingIntent.getService(
                    this, 1,
                    android.content.Intent(this, BlackScreenService::class.java).apply { action = "AUTH_SUCCESS_STOP" },
                    android.app.PendingIntent.FLAG_IMMUTABLE
                )
                val authIntent = android.content.Intent(this, BiometricAuthActivity::class.java).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("EXTRA_SUCCESS_INTENT", successIntent)
                }
                startActivity(authIntent)
                return START_STICKY
            } else {
                stopSelfAndCleanUp()
                return START_NOT_STICKY
            }
        }
        
        if (intent?.action == "AUTH_SUCCESS_UNLOCK") {
            smartAutomationManager.handleManualDismiss()
            showFloatingBubbleInternal()
            return START_STICKY
        }
        isRunning = true"""
content = re.sub(pattern_stop, replacement_stop, content, flags=re.DOTALL)

# Fix 2: handleUnlockRequest
pattern_unlock = r"if \(config\.isBiometricEnabled\) \{.*?com\.noxscreen\.app\.security\.AuthenticationManager\.startAuthentication\(.*?startActivity\(intent\)\n        \} else \{"
replacement_unlock = """if (config.isBiometricEnabled) {
            com.noxscreen.app.security.AuthenticationManager.setAuthenticating()
            val successIntent = android.app.PendingIntent.getService(
                this, 2,
                android.content.Intent(this, BlackScreenService::class.java).apply { action = "AUTH_SUCCESS_UNLOCK" },
                android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val authIntent = android.content.Intent(this, BiometricAuthActivity::class.java).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("EXTRA_SUCCESS_INTENT", successIntent)
            }
            startActivity(authIntent)
        } else {"""
content = re.sub(pattern_unlock, replacement_unlock, content, flags=re.DOTALL)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
