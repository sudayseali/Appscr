import sys

with open('app/src/main/java/com/noxscreen/app/BiometricAuthActivity.kt', 'r') as f:
    content = f.read()

target = """                    val intent = Intent(this@BiometricAuthActivity, BlackScreenService::class.java).apply {
                        action = "BIOMETRIC_SUCCESS"
                    }
                    startService(intent)"""

replacement = """                    val intent = Intent(this@BiometricAuthActivity, BlackScreenService::class.java).apply {
                        action = "BIOMETRIC_SUCCESS"
                    }
                    startService(intent)
                    
                    val broadcastIntent = Intent("com.noxscreen.app.BIOMETRIC_SUCCESS")
                    sendBroadcast(broadcastIntent)"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/BiometricAuthActivity.kt', 'w') as f:
    f.write(content)
