import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """                        ExpandableConfigSection(
                title = "Smart Triggers","""

replacement = """                        ExpandableConfigSection(
                title = "Usage Limits (Focus Mode)",
                icon = Icons.Default.Timer,
                isExpanded = false
            ) {
                var hasUsageStatsPermission by remember { 
                    mutableStateOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZenithAccent),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text("Grant Usage Access", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    ZenithSwitchRow("Enable App Limits", "Block specific apps after time limit", autoConfig.isUsageLimitsEnabled) { 
                        autoConfig = autoConfig.copy(isUsageLimitsEnabled = it)
                        automationSettings.updateConfig(autoConfig) 
                    }
                    if (autoConfig.isUsageLimitsEnabled) {
                        Text("Limit Duration: ${autoConfig.usageLimitDurationMinutes} minutes", color = ZenithSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
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
                                // For now, just hardcode some popular apps to test or open a simple dialog
                                val newBlockedApps = if (autoConfig.blockedApps.isEmpty()) setOf("com.google.android.youtube", "com.android.chrome", "com.facebook.katana", "com.instagram.android") else emptySet<String>()
                                autoConfig = autoConfig.copy(blockedApps = newBlockedApps)
                                automationSettings.updateConfig(autoConfig)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ZenithSecondary.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text(if (autoConfig.blockedApps.isEmpty()) "Block Common Apps (Demo)" else "Unblock All Apps", color = ZenithAccent)
                        }
                        if (autoConfig.blockedApps.isNotEmpty()) {
                            Text("Blocked: ${autoConfig.blockedApps.size} apps", color = ZenithTextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            ExpandableConfigSection(
                title = "Smart Triggers","""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
    f.write(content)
