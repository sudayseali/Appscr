import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """                ZenithSwitchRow("Pocket Mode", "Auto-lock in pocket", autoConfig.isPocketModeEnabled) { 
                    autoConfig = autoConfig.copy(isPocketModeEnabled = it); automationSettings.updateConfig(autoConfig) 
                }"""

replacement = """                ZenithSwitchRow("Pocket Mode", "Auto-lock in pocket", autoConfig.isPocketModeEnabled) { 
                    autoConfig = autoConfig.copy(isPocketModeEnabled = it); automationSettings.updateConfig(autoConfig) 
                }
                
                // Sleep Timer (Battery Saver)
                ZenithSwitchRow("Sleep Timer (Battery Saver)", "Turn off screen completely after time", autoConfig.isSleepTimerEnabled) {
                    autoConfig = autoConfig.copy(isSleepTimerEnabled = it)
                    automationSettings.updateConfig(autoConfig)
                }
                if (autoConfig.isSleepTimerEnabled) {
                    Text("Time to sleep: ${autoConfig.sleepTimerDurationMinutes} minutes", color = ZenithSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                    Slider(
                        value = autoConfig.sleepTimerDurationMinutes.toFloat(),
                        onValueChange = { 
                            autoConfig = autoConfig.copy(sleepTimerDurationMinutes = it.toInt())
                            automationSettings.updateConfig(autoConfig)
                        },
                        valueRange = 1f..120f,
                        steps = 118,
                        colors = SliderDefaults.colors(
                            thumbColor = ZenithAccent,
                            activeTrackColor = ZenithAccent,
                            inactiveTrackColor = ZenithSecondary.copy(alpha = 0.3f)
                        )
                    )
                }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Replaced!")
else:
    print("Not found")
