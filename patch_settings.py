import sys

with open('app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt', 'r') as f:
    content = f.read()

target1 = """    val blockedApps: Set<String> = emptySet()
)"""
replacement1 = """    val blockedApps: Set<String> = emptySet(),
    val unlockedStyles: Set<String> = setOf("lock", "moon", "circle", "power")
)"""

target2 = """            usageLimitDurationMinutes = prefs.getInt("usage_limit_duration_min", 15),
            blockedApps = prefs.getStringSet("blocked_apps", emptySet()) ?: emptySet()
        )"""
replacement2 = """            usageLimitDurationMinutes = prefs.getInt("usage_limit_duration_min", 15),
            blockedApps = prefs.getStringSet("blocked_apps", emptySet()) ?: emptySet(),
            unlockedStyles = prefs.getStringSet("unlocked_styles", setOf("lock", "moon", "circle", "power")) ?: setOf("lock", "moon", "circle", "power")
        )"""

target3 = """            .putStringSet("blocked_apps", config.blockedApps)
            .apply()"""
replacement3 = """            .putStringSet("blocked_apps", config.blockedApps)
            .putStringSet("unlocked_styles", config.unlockedStyles)
            .apply()"""

if target1 in content and target2 in content and target3 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    content = content.replace(target3, replacement3)
    with open('app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt', 'w') as f:
        f.write(content)
    print("Replaced settings!")
else:
    print("Not found")
