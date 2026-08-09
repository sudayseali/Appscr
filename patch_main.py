import re

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "r") as f:
    content = f.read()

# We want to replace the block starting from:
#                     if (autoConfig.isUsageLimitsEnabled) {
# ... down to the end of that block.

new_block = """
                    ZenithSwitchRow(
                        title = "Enable App Limits",
                        subtitle = "Lock distraction apps when limit is reached",
                        checked = autoConfig.isUsageLimitsEnabled
                    ) {
                        autoConfig = autoConfig.copy(isUsageLimitsEnabled = it)
                        automationSettings.updateConfig(autoConfig)
                    }

                    ZenithSwitchRow(
                        title = "Enable Schedule Limits",
                        subtitle = "Lock distraction apps during scheduled times",
                        checked = autoConfig.isScheduleEnabled
                    ) {
                        autoConfig = autoConfig.copy(isScheduleEnabled = it)
                        automationSettings.updateConfig(autoConfig)
                    }

                    if (autoConfig.isUsageLimitsEnabled) {
                        Text(
                            text = "Limit: ${autoConfig.usageLimitDurationMinutes} minutes",
                            color = ZenithSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                        Slider(
                            value = autoConfig.usageLimitDurationMinutes.toFloat(),
                            onValueChange = {
                                autoConfig = autoConfig.copy(usageLimitDurationMinutes = it.toInt())
                                automationSettings.updateConfig(autoConfig)
                            },
                            valueRange = 1f..120f,
                            steps = 118,
                            colors = SliderDefaults.colors(
                                thumbColor = ZenithAccent,
                                activeTrackColor = ZenithAccent,
                                inactiveTrackColor = ZenithCardBorder
                            )
                        )
                    }

                    if (autoConfig.isScheduleEnabled) {
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                                Text("Start Time", color = ZenithTextMuted, fontSize = 12.sp)
                                Button(
                                    onClick = {
                                        android.app.TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                autoConfig = autoConfig.copy(scheduleStartTimeHour = hour, scheduleStartTimeMinute = minute)
                                                automationSettings.updateConfig(autoConfig)
                                            },
                                            autoConfig.scheduleStartTimeHour,
                                            autoConfig.scheduleStartTimeMinute,
                                            true
                                        ).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ZenithSecondary.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = String.format("%02d:%02d", autoConfig.scheduleStartTimeHour, autoConfig.scheduleStartTimeMinute),
                                        color = ZenithText
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                                Text("End Time", color = ZenithTextMuted, fontSize = 12.sp)
                                Button(
                                    onClick = {
                                        android.app.TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                autoConfig = autoConfig.copy(scheduleEndTimeHour = hour, scheduleEndTimeMinute = minute)
                                                automationSettings.updateConfig(autoConfig)
                                            },
                                            autoConfig.scheduleEndTimeHour,
                                            autoConfig.scheduleEndTimeMinute,
                                            true
                                        ).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ZenithSecondary.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = String.format("%02d:%02d", autoConfig.scheduleEndTimeHour, autoConfig.scheduleEndTimeMinute),
                                        color = ZenithText
                                    )
                                }
                            }
                        }
                    }

                    if (autoConfig.isUsageLimitsEnabled || autoConfig.isScheduleEnabled) {
                        var showAppSelection by remember { mutableStateOf(false) }

                        if (showAppSelection) {
                            com.noxscreen.app.ui.AppSelectionDialog(
                                initialSelectedApps = autoConfig.blockedApps,
                                onDismissRequest = { showAppSelection = false },
                                onAppsSelected = { apps ->
                                    autoConfig = autoConfig.copy(blockedApps = apps)
                                    automationSettings.updateConfig(autoConfig)
                                    showAppSelection = false
                                }
                            )
                        }

                        Button(
                            onClick = { showAppSelection = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ZenithSecondary.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        ) {
                            Text(
                                text = "Select Apps to Block",
                                color = ZenithAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (autoConfig.blockedApps.isNotEmpty()) {
                            Text(
                                text = "Blocked: ${autoConfig.blockedApps.size} apps",
                                color = ZenithTextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
"""

# Regex to match the block starting from ZenithSwitchRow("Enable App Limits") up to the end of the else block.
pattern = re.compile(
    r'ZenithSwitchRow\(\s*title = "Enable App Limits".*?if \(autoConfig\.blockedApps\.isNotEmpty\(\)\) \{.*?\n\s*\}\s*\}',
    re.DOTALL
)

new_content = pattern.sub(new_block, content)

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "w") as f:
    f.write(new_content)

print("Patched.")
