import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """            ExpandableConfigSection(
                title = "Usage Limits",
                icon = Icons.Default.HealthAndSafety,
                isExpanded = false
            ) {
                Text("Helpful for digital wellbeing", color = ZenithTextMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                ZenithSwitchRow("Daily Limit (1hr)", "Stop overlay after limit", false) { }
                ZenithSwitchRow("Bedtime Mode", "Auto-trigger at 10 PM", false) { }
            }"""

replacement = """            ExpandableConfigSection(
                title = "Usage Limits (Focus Mode)",
                icon = Icons.Default.HealthAndSafety,
                isExpanded = false
            ) {
                var hasUsageStatsPermission by remember { 
                    mutableStateOf(
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
                            val mode = appOps.checkOpNoThrow(
                                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, 
                                android.os.Process.myUid(), 
                                context.packageName
                            )
                            mode == android.app.AppOpsManager.MODE_ALLOWED
                        } else {
                            true
                        }
                    ) 
                }

                if (!hasUsageStatsPermission) {
                    Button(
                        onClick = {
                            context.startActivity(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZenithAccent),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text("Grant Usage Access", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    ZenithSwitchRow("Enable App Limits", "Block apps when time runs out", autoConfig.isUsageLimitsEnabled) { 
                        autoConfig = autoConfig.copy(isUsageLimitsEnabled = it)
                        automationSettings.updateConfig(autoConfig) 
                    }
                    if (autoConfig.isUsageLimitsEnabled) {
                        Text("Limit: ${autoConfig.usageLimitDurationMinutes} minutes", color = ZenithSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                        Slider(
                            value = autoConfig.usageLimitDurationMinutes.toFloat(),
                            onValueChange = { 
                                autoConfig = autoConfig.copy(usageLimitDurationMinutes = it.toInt())
                            },
                            onValueChangeFinished = {
                                automationSettings.updateConfig(autoConfig)
                            },
                            valueRange = 1f..120f,
                            steps = 119,
                            colors = SliderDefaults.colors(
                                thumbColor = ZenithAccent,
                                activeTrackColor = ZenithAccent,
                                inactiveTrackColor = ZenithSecondary.copy(alpha = 0.3f)
                            )
                        )
                        
                        Button(
                            onClick = {
                                val newBlockedApps = if (autoConfig.blockedApps.isEmpty()) {
                                    setOf("com.google.android.youtube", "com.android.chrome", "com.facebook.katana", "com.instagram.android")
                                } else {
                                    emptySet<String>()
                                }
                                autoConfig = autoConfig.copy(blockedApps = newBlockedApps)
                                automationSettings.updateConfig(autoConfig)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ZenithSecondary.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text(if (autoConfig.blockedApps.isEmpty()) "Block Common Apps" else "Unblock All Apps", color = ZenithAccent)
                        }
                        if (autoConfig.blockedApps.isNotEmpty()) {
                            Text("Blocked: ${autoConfig.blockedApps.size} apps", color = ZenithTextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Replaced!")
else:
    print("Target not found. Doing fallback.")

