import sys

with open('app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt', 'r') as f:
    content = f.read()

target_fields = """    val reduceBrightness: Boolean = false,
    val oledBurnInProtection: Boolean = true
)"""
replacement_fields = """    val reduceBrightness: Boolean = false,
    val oledBurnInProtection: Boolean = true,
    val isBiometricEnabled: Boolean = false
)"""
content = content.replace(target_fields, replacement_fields)

target_get = """            reduceBrightness = prefs.getBoolean("reduce_brightness", false),
            oledBurnInProtection = prefs.getBoolean("oled_burn_in_protection", true)
        )"""
replacement_get = """            reduceBrightness = prefs.getBoolean("reduce_brightness", false),
            oledBurnInProtection = prefs.getBoolean("oled_burn_in_protection", true),
            isBiometricEnabled = prefs.getBoolean("is_biometric_enabled", false)
        )"""
content = content.replace(target_get, replacement_get)

target_put = """            .putBoolean("reduce_brightness", config.reduceBrightness)
            .putBoolean("oled_burn_in_protection", config.oledBurnInProtection)
            .apply()"""
replacement_put = """            .putBoolean("reduce_brightness", config.reduceBrightness)
            .putBoolean("oled_burn_in_protection", config.oledBurnInProtection)
            .putBoolean("is_biometric_enabled", config.isBiometricEnabled)
            .apply()"""
content = content.replace(target_put, replacement_put)

with open('app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt', 'w') as f:
    f.write(content)
