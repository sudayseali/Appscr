import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """                // Wake gesture selector
                Text("Wake Gesture (Taps)", color = ZenithSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))"""

replacement = """                
                Text("Floating Lock Style", color = ZenithSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val styles = listOf("lock" to androidx.compose.ui.res.painterResource(R.drawable.ic_lock),
                        "moon" to androidx.compose.ui.res.painterResource(R.drawable.ic_moon),
                        "circle" to androidx.compose.ui.res.painterResource(R.drawable.ic_circle),
                        "double_circle" to androidx.compose.ui.res.painterResource(R.drawable.ic_double_circle),
                        "key" to androidx.compose.ui.res.painterResource(R.drawable.ic_key),
                        "eye_off" to androidx.compose.ui.res.painterResource(R.drawable.ic_eye_off))
                        
                    items(styles.size) { index ->
                        val (styleName, painter) = styles[index]
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ZenithCard)
                                .border(
                                    2.dp,
                                    if (autoConfig.floatingLockStyle == styleName) ZenithAccent else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { 
                                    autoConfig = autoConfig.copy(floatingLockStyle = styleName)
                                    automationSettings.updateConfig(autoConfig) 
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(painter, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                
                Text("Floating Lock Size", color = ZenithSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp))
                androidx.compose.material3.Slider(
                    value = autoConfig.floatingLockSize,
                    onValueChange = { 
                        autoConfig = autoConfig.copy(floatingLockSize = it)
                        automationSettings.updateConfig(autoConfig)
                    },
                    valueRange = 0.5f..2.0f,
                    colors = androidx.compose.material3.SliderDefaults.colors(
                        thumbColor = ZenithAccent,
                        activeTrackColor = ZenithAccent,
                        inactiveTrackColor = ZenithCard
                    )
                )

                // Wake gesture selector
                Text("Wake Gesture (Taps)", color = ZenithSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
    f.write(content)
