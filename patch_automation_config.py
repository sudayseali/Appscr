import sys

with open('app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt', 'r') as f:
    content = f.read()

target1 = """    val isBiometricEnabled: Boolean = false
)"""
replacement1 = """    val isBiometricEnabled: Boolean = false,
    val isUsageLimitsEnabled: Boolean = false,
    val usageLimitDurationMinutes: Int = 15,
    val blockedApps: Set<String> = emptySet()
)"""
content = content.replace(target1, replacement1)

target2 = """            isBiometricEnabled = prefs.getBoolean("is_biometric_enabled", false)
        )"""
replacement2 = """            isBiometricEnabled = prefs.getBoolean("is_biometric_enabled", false),
            isUsageLimitsEnabled = prefs.getBoolean("is_usage_limits_enabled", false),
            usageLimitDurationMinutes = prefs.getInt("usage_limit_duration_min", 15),
            blockedApps = prefs.getStringSet("blocked_apps", emptySet()) ?: emptySet()
        )"""
content = content.replace(target2, replacement2)

target3 = """            .putBoolean("is_biometric_enabled", config.isBiometricEnabled)
            .apply()"""
replacement3 = """            .putBoolean("is_biometric_enabled", config.isBiometricEnabled)
            .putBoolean("is_usage_limits_enabled", config.isUsageLimitsEnabled)
            .putInt("usage_limit_duration_min", config.usageLimitDurationMinutes)
            .putStringSet("blocked_apps", config.blockedApps)
            .apply()"""
content = content.replace(target3, replacement3)

with open('app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt', 'w') as f:
    f.write(content)
