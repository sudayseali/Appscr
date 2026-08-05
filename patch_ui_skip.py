with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "r", encoding="utf-8") as f:
    content = f.read()

target_ui = """                ZenithSwitchRow(stringResource(R.string.privacy_tint), "Dim screen instead of total black", autoConfig.isDarkTintEnabled) { 
                    autoConfig = autoConfig.copy(isDarkTintEnabled = it); automationSettings.updateConfig(autoConfig) 
                }"""

replacement_ui = """                ZenithSwitchRow(stringResource(R.string.privacy_tint), "Dim screen instead of total black", autoConfig.isDarkTintEnabled) { 
                    autoConfig = autoConfig.copy(isDarkTintEnabled = it); automationSettings.updateConfig(autoConfig) 
                }
                ZenithSwitchRow(stringResource(R.string.skip_unlock_screen), "Directly unlock the screen on tap", autoConfig.isSkipUnlockScreenEnabled) { 
                    autoConfig = autoConfig.copy(isSkipUnlockScreenEnabled = it); automationSettings.updateConfig(autoConfig) 
                }"""

if 'isSkipUnlockScreenEnabled' not in content:
    content = content.replace(target_ui, replacement_ui)

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "w", encoding="utf-8") as f:
    f.write(content)
