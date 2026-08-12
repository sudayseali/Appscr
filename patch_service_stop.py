import re

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

stop_logic = """
    private fun stopSelfAndCleanUp() {
        isRunning = false
        updateTile(this)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            val config = smartAutomationManager.settings.getConfig()
            if (config.isBiometricEnabled) {
                com.noxscreen.app.security.AuthenticationManager.startAuthentication(
                    onSuccess = {
                        stopSelfAndCleanUp()
                    },
                    onFailure = {
                        // User cancelled or failed to authenticate to stop the service, keep running
                    }
                )
                val authIntent = android.content.Intent(this, BiometricAuthActivity::class.java)
                authIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(authIntent)
                return START_STICKY
            } else {
                stopSelfAndCleanUp()
                return START_NOT_STICKY
            }
        }
"""

content = re.sub(
    r"""    override fun onStartCommand\(intent: Intent\?, flags: Int, startId: Int\): Int \{\s*if \(intent\?\.action == "STOP_SERVICE"\) \{\s*isRunning = false\s*updateTile\(this\)\s*if \(android\.os\.Build\.VERSION\.SDK_INT >= android\.os\.Build\.VERSION_CODES\.N\) \{\s*stopForeground\(STOP_FOREGROUND_REMOVE\)\s*\} else \{\s*@Suppress\("DEPRECATION"\)\s*stopForeground\(true\)\s*\}\s*stopSelf\(\)\s*return START_NOT_STICKY\s*\}""",
    stop_logic,
    content
)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
