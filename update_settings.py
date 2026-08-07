with open("app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt", "r") as f:
    content = f.read()

# in AutomationConfig
content = content.replace("val isPocketModeEnabled: Boolean = false", "val isPocketModeEnabled: Boolean = true")
content = content.replace("val isFlipToSleepEnabled: Boolean = false", "val isFlipToSleepEnabled: Boolean = true")
content = content.replace("val isShakeToWakeEnabled: Boolean = false", "val isShakeToWakeEnabled: Boolean = true")
content = content.replace("val isAodEnabled: Boolean = false", "val isAodEnabled: Boolean = true")
content = content.replace("val oledBurnInProtection: Boolean = false", "val oledBurnInProtection: Boolean = true")
content = content.replace("val isSkipUnlockScreenEnabled: Boolean = false", "val isSkipUnlockScreenEnabled: Boolean = true")

# in getConfig
content = content.replace("isPocketModeEnabled = prefs.getBoolean(\"is_pocket_mode_enabled\", false)", "isPocketModeEnabled = prefs.getBoolean(\"is_pocket_mode_enabled\", true)")
content = content.replace("isFlipToSleepEnabled = prefs.getBoolean(\"is_flip_to_sleep_enabled\", false)", "isFlipToSleepEnabled = prefs.getBoolean(\"is_flip_to_sleep_enabled\", true)")
content = content.replace("isShakeToWakeEnabled = prefs.getBoolean(\"is_shake_to_wake_enabled\", false)", "isShakeToWakeEnabled = prefs.getBoolean(\"is_shake_to_wake_enabled\", true)")
content = content.replace("isAodEnabled = prefs.getBoolean(\"is_aod_enabled\", false)", "isAodEnabled = prefs.getBoolean(\"is_aod_enabled\", true)")
content = content.replace("oledBurnInProtection = prefs.getBoolean(\"oled_burn_in_protection\", false)", "oledBurnInProtection = prefs.getBoolean(\"oled_burn_in_protection\", true)")
content = content.replace("isSkipUnlockScreenEnabled = prefs.getBoolean(\"is_skip_unlock_screen_enabled\", false)", "isSkipUnlockScreenEnabled = prefs.getBoolean(\"is_skip_unlock_screen_enabled\", true)")

with open("app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt", "w") as f:
    f.write(content)
