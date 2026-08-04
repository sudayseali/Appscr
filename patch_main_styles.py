import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """                        Box(
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
                        }"""

replacement = """                        val isUnlocked = autoConfig.unlockedStyles.contains(styleName)
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val activity = context as? android.app.Activity
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
                                    if (isUnlocked) {
                                        autoConfig = autoConfig.copy(floatingLockStyle = styleName)
                                        automationSettings.updateConfig(autoConfig)
                                    } else {
                                        activity?.let {
                                            adsManager.showRewardedAd(it) {
                                                val newUnlocked = autoConfig.unlockedStyles + styleName
                                                autoConfig = autoConfig.copy(floatingLockStyle = styleName, unlockedStyles = newUnlocked)
                                                automationSettings.updateConfig(autoConfig)
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(painter, contentDescription = null, tint = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(24.dp))
                            if (!isUnlocked) {
                                Icon(androidx.compose.material.icons.Icons.Default.Lock, contentDescription = "Locked", tint = ZenithAccent, modifier = Modifier.size(16.dp).align(Alignment.BottomEnd))
                            }
                        }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Replaced Main styles!")
else:
    print("Main styles target not found")
