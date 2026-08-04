import re

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r', encoding='utf-8') as f:
    content = f.read()

gamification_composable = """@Composable
fun GamificationSection(totalTimeSaved: Long) {
    val totalHours = (totalTimeSaved / 3600000).toFloat()
    
    // Level calculation: 1 hour = Level 1, 5 hours = Level 2, 10 hours = Level 3, 20 hours = Level 4...
    val (level, currentLevelThreshold, nextLevelThreshold) = when {
        totalHours < 1 -> Triple(0, 0f, 1f)
        totalHours < 5 -> Triple(1, 1f, 5f)
        totalHours < 10 -> Triple(2, 5f, 10f)
        totalHours < 25 -> Triple(3, 10f, 25f)
        totalHours < 50 -> Triple(4, 25f, 50f)
        totalHours < 100 -> Triple(5, 50f, 100f)
        else -> Triple(6, 100f, 200f) // Keep extending as needed
    }
    
    val progress = ((totalHours - currentLevelThreshold) / (nextLevelThreshold - currentLevelThreshold)).coerceIn(0f, 1f)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF131422), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF2A2E44), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Level $level",
                    color = ZenithAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${String.format("%.1f", totalHours)} Hours Saved",
                    color = ZenithTextMuted,
                    fontSize = 14.sp
                )
            }
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Trophy",
                tint = if (level > 0) Color(0xFFFFD700) else Color.Gray,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = ZenithAccent,
            trackColor = Color(0xFF2A2E44)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${currentLevelThreshold.toInt()}h", color = ZenithTextMuted, fontSize = 12.sp)
            Text("${nextLevelThreshold.toInt()}h to next", color = ZenithTextMuted, fontSize = 12.sp)
        }
    }
}"""

if 'fun GamificationSection' not in content:
    # insert before @Composable fun ZenithSwitchRow
    content = content.replace('@Composable\nfun ZenithSwitchRow', gamification_composable + '\n\n@Composable\nfun ZenithSwitchRow')

gamification_section = """            Spacer(modifier = Modifier.height(16.dp))
            
            ExpandableConfigSection(
                title = stringResource(R.string.battery_analytics),
                icon = Icons.Default.Analytics,
                isExpanded = true
            ) {
                GamificationSection(totalTimeSaved)
            }
"""

if 'GamificationSection(totalTimeSaved)' not in content:
    # insert after usage_limits ExpandableConfigSection ends
    target = """            ExpandableConfigSection(
                title = stringResource(R.string.usage_limits),
                icon = Icons.Default.HealthAndSafety,
                isExpanded = false
            ) {
                val lifecycleOwner = LocalLifecycleOwner.current
                var localUsageLimit by remember { mutableStateOf(autoConfig.dailyUsageLimitSeconds) }
                
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            localUsageLimit = automationSettings.getConfig().dailyUsageLimitSeconds
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                ZenithSliderRow(
                    title = stringResource(R.string.daily_limit),
                    subtitle = if (localUsageLimit > 0) "${localUsageLimit / 3600}h ${(localUsageLimit % 3600) / 60}m" else "No limit",
                    value = localUsageLimit.toFloat(),
                    valueRange = 0f..28800f,
                    steps = 47,
                    onValueChange = { newValue ->
                        localUsageLimit = newValue.toInt()
                    },
                    onValueChangeFinished = {
                        autoConfig = autoConfig.copy(dailyUsageLimitSeconds = localUsageLimit)
                        automationSettings.updateConfig(autoConfig)
                        com.noxscreen.app.automation.UsageLimitMonitor(context).updateLimitFromConfig()
                    }
                )
            }"""
    
    content = content.replace(target, target + "\n" + gamification_section)


with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w', encoding='utf-8') as f:
    f.write(content)
