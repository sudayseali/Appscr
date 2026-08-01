import sys

with open('app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt', 'r') as f:
    content = f.read()

target = """class AutomationSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("NoxAutomationPrefs", Context.MODE_PRIVATE)"""

replacement = """class AutomationSettings(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("NoxAutomationPrefs", Context.MODE_PRIVATE)"""
content = content.replace(target, replacement)

target2 = """            .putBoolean("reduce_brightness", config.reduceBrightness)
            .putBoolean("oled_burn_in_protection", config.oledBurnInProtection)
            .putBoolean("is_biometric_enabled", config.isBiometricEnabled)
            .apply()
    }"""
replacement2 = """            .putBoolean("reduce_brightness", config.reduceBrightness)
            .putBoolean("oled_burn_in_protection", config.oledBurnInProtection)
            .putBoolean("is_biometric_enabled", config.isBiometricEnabled)
            .apply()
            
        val intent = android.content.Intent("com.noxscreen.app.SETTINGS_UPDATED")
        context.sendBroadcast(intent)
    }"""
content = content.replace(target2, replacement2)

with open('app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt', 'w') as f:
    f.write(content)
