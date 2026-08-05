with open("app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt", "r", encoding="utf-8") as f:
    content = f.read()

target = """    val isPocketModeEnabled: Boolean = false,
    val isMotionDetectionEnabled: Boolean = false,
    val stationaryDurationSeconds: Int = 10,"""
replacement = """    val isPocketModeEnabled: Boolean = false,
    val isMotionDetectionEnabled: Boolean = false,
    val isFlipToSleepEnabled: Boolean = false,
    val isShakeToWakeEnabled: Boolean = false,
    val stationaryDurationSeconds: Int = 10,"""

if 'isFlipToSleepEnabled' not in content:
    content = content.replace(target, replacement)

target2 = """            isMotionDetectionEnabled = prefs.getBoolean("isMotionDetectionEnabled", false),
            stationaryDurationSeconds = prefs.getInt("stationaryDurationSeconds", 10),"""
replacement2 = """            isMotionDetectionEnabled = prefs.getBoolean("isMotionDetectionEnabled", false),
            isFlipToSleepEnabled = prefs.getBoolean("isFlipToSleepEnabled", false),
            isShakeToWakeEnabled = prefs.getBoolean("isShakeToWakeEnabled", false),
            stationaryDurationSeconds = prefs.getInt("stationaryDurationSeconds", 10),"""

if 'isFlipToSleepEnabled = prefs' not in content:
    content = content.replace(target2, replacement2)

target3 = """            putBoolean("isMotionDetectionEnabled", config.isMotionDetectionEnabled)
            putInt("stationaryDurationSeconds", config.stationaryDurationSeconds)"""
replacement3 = """            putBoolean("isMotionDetectionEnabled", config.isMotionDetectionEnabled)
            putBoolean("isFlipToSleepEnabled", config.isFlipToSleepEnabled)
            putBoolean("isShakeToWakeEnabled", config.isShakeToWakeEnabled)
            putInt("stationaryDurationSeconds", config.stationaryDurationSeconds)"""

if 'putBoolean("isFlipToSleepEnabled"' not in content:
    content = content.replace(target3, replacement3)

with open("app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt", "w", encoding="utf-8") as f:
    f.write(content)
