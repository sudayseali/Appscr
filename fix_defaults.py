with open("app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt", "r") as f:
    content = f.read()

# Revert to false
content = content.replace("val isPocketModeEnabled: Boolean = true", "val isPocketModeEnabled: Boolean = false")
content = content.replace("val isFlipToSleepEnabled: Boolean = true", "val isFlipToSleepEnabled: Boolean = false")
content = content.replace("val isShakeToWakeEnabled: Boolean = true", "val isShakeToWakeEnabled: Boolean = false")
content = content.replace("val isAodEnabled: Boolean = true", "val isAodEnabled: Boolean = false")
content = content.replace("val oledBurnInProtection: Boolean = true", "val oledBurnInProtection: Boolean = false")
content = content.replace("val isSkipUnlockScreenEnabled: Boolean = true", "val isSkipUnlockScreenEnabled: Boolean = false")

# Also in getConfig
content = content.replace("isPocketModeEnabled = prefs.getBoolean(\"is_pocket_mode_enabled\", true)", "isPocketModeEnabled = prefs.getBoolean(\"is_pocket_mode_enabled\", false)")
content = content.replace("isFlipToSleepEnabled = prefs.getBoolean(\"is_flip_to_sleep_enabled\", true)", "isFlipToSleepEnabled = prefs.getBoolean(\"is_flip_to_sleep_enabled\", false)")
content = content.replace("isShakeToWakeEnabled = prefs.getBoolean(\"is_shake_to_wake_enabled\", true)", "isShakeToWakeEnabled = prefs.getBoolean(\"is_shake_to_wake_enabled\", false)")
content = content.replace("isAodEnabled = prefs.getBoolean(\"is_aod_enabled\", true)", "isAodEnabled = prefs.getBoolean(\"is_aod_enabled\", false)")
content = content.replace("oledBurnInProtection = prefs.getBoolean(\"oled_burn_in_protection\", true)", "oledBurnInProtection = prefs.getBoolean(\"oled_burn_in_protection\", false)")
content = content.replace("isSkipUnlockScreenEnabled = prefs.getBoolean(\"is_skip_unlock_screen_enabled\", true)", "isSkipUnlockScreenEnabled = prefs.getBoolean(\"is_skip_unlock_screen_enabled\", false)")

with open("app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt", "w") as f:
    f.write(content)
