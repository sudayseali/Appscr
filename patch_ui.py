with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "r", encoding="utf-8") as f:
    content = f.read()

target_ui = """                ZenithSwitchRow(stringResource(R.string.pocket_mode), "Auto-lock in pocket", autoConfig.isPocketModeEnabled) { 
                    autoConfig = autoConfig.copy(isPocketModeEnabled = it); automationSettings.updateConfig(autoConfig) 
                }"""

replacement_ui = """                ZenithSwitchRow(stringResource(R.string.pocket_mode), "Auto-lock in pocket", autoConfig.isPocketModeEnabled) { 
                    autoConfig = autoConfig.copy(isPocketModeEnabled = it); automationSettings.updateConfig(autoConfig) 
                }
                
                ZenithSwitchRow(stringResource(R.string.flip_to_sleep), "Turn face down to lock", autoConfig.isFlipToSleepEnabled) { 
                    autoConfig = autoConfig.copy(isFlipToSleepEnabled = it); automationSettings.updateConfig(autoConfig) 
                }
                
                ZenithSwitchRow(stringResource(R.string.shake_to_wake), "Shake device to unlock", autoConfig.isShakeToWakeEnabled) { 
                    autoConfig = autoConfig.copy(isShakeToWakeEnabled = it); automationSettings.updateConfig(autoConfig) 
                }"""

if 'isFlipToSleepEnabled' not in content:
    content = content.replace(target_ui, replacement_ui)

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "w", encoding="utf-8") as f:
    f.write(content)
