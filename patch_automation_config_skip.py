with open("app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt", "r", encoding="utf-8") as f:
    content = f.read()

target = """    val hideFloatingButton: Boolean = false,
    val reduceBrightness: Boolean = false,
    val oledBurnInProtection: Boolean = true,"""
replacement = """    val hideFloatingButton: Boolean = false,
    val reduceBrightness: Boolean = false,
    val oledBurnInProtection: Boolean = true,
    val isSkipUnlockScreenEnabled: Boolean = false,"""

if 'isSkipUnlockScreenEnabled' not in content:
    content = content.replace(target, replacement)

target2 = """            hideFloatingButton = prefs.getBoolean("hideFloatingButton", false),
            reduceBrightness = prefs.getBoolean("reduceBrightness", false),
            oledBurnInProtection = prefs.getBoolean("oledBurnInProtection", true),"""
replacement2 = """            hideFloatingButton = prefs.getBoolean("hideFloatingButton", false),
            reduceBrightness = prefs.getBoolean("reduceBrightness", false),
            oledBurnInProtection = prefs.getBoolean("oledBurnInProtection", true),
            isSkipUnlockScreenEnabled = prefs.getBoolean("isSkipUnlockScreenEnabled", false),"""

if 'isSkipUnlockScreenEnabled = prefs' not in content:
    content = content.replace(target2, replacement2)

target3 = """            putBoolean("hideFloatingButton", config.hideFloatingButton)
            putBoolean("reduceBrightness", config.reduceBrightness)
            putBoolean("oledBurnInProtection", config.oledBurnInProtection)"""
replacement3 = """            putBoolean("hideFloatingButton", config.hideFloatingButton)
            putBoolean("reduceBrightness", config.reduceBrightness)
            putBoolean("oledBurnInProtection", config.oledBurnInProtection)
            putBoolean("isSkipUnlockScreenEnabled", config.isSkipUnlockScreenEnabled)"""

if 'putBoolean("isSkipUnlockScreenEnabled"' not in content:
    content = content.replace(target3, replacement3)

with open("app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt", "w", encoding="utf-8") as f:
    f.write(content)
