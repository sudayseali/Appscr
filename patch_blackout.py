import re

with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'r') as f:
    content = f.read()

# Remove the BroadcastReceiver from BlackoutActivity
pattern_receiver = r"""    private val receiver = object : BroadcastReceiver\(\) \{.*?\}\s*\}"""
content = re.sub(pattern_receiver, "", content, flags=re.DOTALL)

pattern_ondestroy = r"""    override fun onDestroy\(\) \{.*?\}\s*\}"""
content = re.sub(pattern_ondestroy, "", content, flags=re.DOTALL)

# Remove registerReceiver logic from onCreate
pattern_register = r"""        val filter = IntentFilter\("com\.noxscreen\.app\.BIOMETRIC_SUCCESS"\).*?        \}"""
content = re.sub(pattern_register, "", content, flags=re.DOTALL)

# Refactor onUnlock logic
pattern_onunlock = r"""                    val settings = com\.noxscreen\.app\.automation\.AutomationSettings\(this\)\s*val isBiometricEnabled = settings\.getConfig\(\)\.isBiometricEnabled\s*if \(isBiometricEnabled\) \{\s*val intent = Intent\(this, BiometricAuthActivity::class\.java\)\s*intent\.addFlags\(Intent\.FLAG_ACTIVITY_NEW_TASK or Intent\.FLAG_ACTIVITY_CLEAR_TOP\)\s*startActivity\(intent\)\s*\} else \{\s*finish\(\)\s*\}"""

new_onunlock = """                    val settings = com.noxscreen.app.automation.AutomationSettings(this)
                    val isBiometricEnabled = settings.getConfig().isBiometricEnabled
                    if (isBiometricEnabled) {
                        com.noxscreen.app.security.AuthenticationManager.startAuthentication(
                            onSuccess = { finish() },
                            onFailure = { /* keep blackout screen active */ }
                        )
                        val intent = android.content.Intent(this, BiometricAuthActivity::class.java)
                        startActivity(intent)
                    } else {
                        finish()
                    }"""

content = content.replace("                    val settings = com.noxscreen.app.automation.AutomationSettings(this)\n                    val isBiometricEnabled = settings.getConfig().isBiometricEnabled\n                    if (isBiometricEnabled) {\n                        val intent = Intent(this, BiometricAuthActivity::class.java)\n                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)\n                        startActivity(intent)\n                    } else {\n                        finish()\n                     }", new_onunlock)

with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'w') as f:
    f.write(content)
