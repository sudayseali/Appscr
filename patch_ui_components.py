import re

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "r") as f:
    content = f.read()

# 1. Colors
colors_target = r'val ZenithBackground = Color.*?val ZenithTextMuted = Color\(0xFF7A8DAB\)'
colors_replacement = """val ZenithBackgroundStart = Color(0xFF00050A)
val ZenithBackgroundEnd = Color(0xFF0A0F1A)
val ZenithCard = Color(0xFF131722)
val ZenithAccent = Color(0xFF00E676)
val ZenithSecondary = Color(0xFF7B61FF)
val ZenithTextMuted = Color(0xFF8B92A5)"""
content = re.sub(colors_target, colors_replacement, content, flags=re.DOTALL)

# 2. Main screen background and padding
target_background = r'Box\(modifier = Modifier\.fillMaxSize\(\)\.background\(ZenithBackground\)\) \{'
replacement_background = """Box(modifier = Modifier.fillMaxSize().background(
        brush = Brush.verticalGradient(listOf(ZenithBackgroundStart, ZenithBackgroundEnd))
    )) {"""
content = re.sub(target_background, replacement_background, content)

target_padding = r'\.padding\(top = 64\.dp, bottom = 120\.dp, start = 24\.dp, end = 24\.dp\)'
replacement_padding = r'.padding(top = 48.dp, bottom = 120.dp, start = 20.dp, end = 20.dp)'
content = re.sub(target_padding, replacement_padding, content)

# 3. Header
target_header = r'// Header.*?Text\(stringResource\(R\.string\.eco_screen_optimizer\)[^\n]*\)'
replacement_header = """// Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(stringResource(R.string.app_name).split(" ")[0], color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text(" Pro", color = ZenithAccent, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(stringResource(R.string.eco_screen_optimizer), color = ZenithTextMuted, fontSize = 11.sp, letterSpacing = 2.sp, modifier = Modifier.padding(top = 4.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Security, contentDescription = "Protected", tint = ZenithAccent, modifier = Modifier.size(24.dp))
                    Text("Protected", color = ZenithTextMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }"""
content = re.sub(target_header, replacement_header, content, flags=re.DOTALL)

# 4. Power Button wrapper glow
target_power = r'// Central Power Button\n\s*PowerPulseButton\(\n\s*onClick = \{\n\s*if \(\!hasPermission\) \{\n\s*onRequestPermission\(\)\n\s*\} else if \(isServiceRunning\) \{\n\s*onStopService\(\)\n\s*\} else \{\n\s*onStartService\(\)\n\s*\}\n\s*\},\n\s*isRunning = isServiceRunning\n\s*\)'

replacement_power = """// Central Power Button
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(Brush.radialGradient(listOf(ZenithAccent.copy(alpha = 0.2f), Color.Transparent)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                PowerPulseButton(
                    onClick = {
                        if (!hasPermission) {
                            onRequestPermission()
                        } else if (isServiceRunning) {
                            onStopService()
                        } else {
                            onStartService()
                        }
                    },
                    isRunning = isServiceRunning
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(if (isServiceRunning) "Tap to wake screen" else "Tap to sleep screen", color = ZenithAccent, fontSize = 14.sp)
            """
content = re.sub(target_power, replacement_power, content, flags=re.DOTALL)

# 5. ImpactCard Row
target_impact_cards = r'Row\(modifier = Modifier\.fillMaxWidth\(\), horizontalArrangement = Arrangement\.SpaceBetween\) \{\n\s*ImpactCard\(stringResource\(R\.string\.energy_saved\), "\$\{estimatedMah\} mAh", Icons\.Default\.Bolt, ZenithAccent, Modifier\.weight\(1f\)\)\n\s*Spacer\(modifier = Modifier\.width\(16\.dp\)\)\n\s*ImpactCard\(stringResource\(R\.string\.usage_count\), "\$usageCount", Icons\.Default\.Analytics, ZenithSecondary, Modifier\.weight\(1f\)\)\n\s*\}'
replacement_impact_cards = """Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ImpactCard(
                    title = stringResource(R.string.energy_saved),
                    value = "${estimatedMah} mAh",
                    icon = Icons.Default.Bolt,
                    color = ZenithAccent,
                    modifier = Modifier.weight(1f)
                )
                
                val formattedTime = String.format("%dh %dm", totalTimeSaved / (1000 * 60 * 60), (totalTimeSaved / (1000 * 60)) % 60)
                ImpactCard(
                    title = "Screen Off",
                    value = formattedTime,
                    icon = Icons.Default.Schedule,
                    color = ZenithSecondary,
                    modifier = Modifier.weight(1f)
                )
            }"""
content = re.sub(target_impact_cards, replacement_impact_cards, content, flags=re.DOTALL)

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "w") as f:
    f.write(content)

