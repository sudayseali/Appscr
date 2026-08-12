import re

with open('/app/applet/app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = r"""                Text\(\s*text = stringResource\(R\.string\.wake_gesture\),\s*color = Color\(0xFF00E5FF\),\s*fontSize = 13\.sp,\s*fontWeight = FontWeight\.Bold,\s*modifier = Modifier\.padding\(top = 14\.dp, bottom = 10\.dp\)\s*\)"""

replacement = """                Text(
                    text = stringResource(R.string.wake_gesture),
                    color = Color(0xFF00E5FF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 14.dp, bottom = 10.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${autoConfig.tapsToWake} taps to wake screen",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { 
                                if (autoConfig.tapsToWake > 1) {
                                    autoConfig = autoConfig.copy(tapsToWake = autoConfig.tapsToWake - 1)
                                    automationSettings.updateConfig(autoConfig)
                                }
                            },
                            modifier = Modifier.size(36.dp).background(Color(0xFF1E293B), CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease taps", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = autoConfig.tapsToWake.toString(),
                            color = Color(0xFF00E5FF),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(
                            onClick = { 
                                if (autoConfig.tapsToWake < 10) {
                                    autoConfig = autoConfig.copy(tapsToWake = autoConfig.tapsToWake + 1)
                                    automationSettings.updateConfig(autoConfig)
                                }
                            },
                            modifier = Modifier.size(36.dp).background(Color(0xFF1E293B), CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase taps", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }"""

if re.search(target, content):
    content = re.sub(target, replacement, content)
    with open('/app/applet/app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Patched successfully")
else:
    print("Target not found")
