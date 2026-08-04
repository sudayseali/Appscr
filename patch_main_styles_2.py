import sys
import re

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """                        val (styleName, painter) = styles[index]
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
                        }"""

# Try exact replacement first
if target in content:
    pass
else:
    # Relaxed search
    pattern = r"val \(styleName, painter\) = styles\[index\]\s*Box\(\s*modifier = Modifier\s*\.size\(56\.dp\)\s*\.clip\(RoundedCornerShape\(12\.dp\)\)\s*\.background\(ZenithCard\)\s*\.border\(\s*2\.dp,\s*if \(autoConfig\.floatingLockStyle == styleName\) ZenithAccent else Color\.Transparent,\s*RoundedCornerShape\(12\.dp\)\s*\)\s*\.clickable \{\s*autoConfig = autoConfig\.copy\(floatingLockStyle = styleName\)\s*automationSettings\.updateConfig\(autoConfig\)\s*\},\s*contentAlignment = Alignment\.Center\s*\)\s*\{\s*Icon\(painter, contentDescription = null, tint = Color\.White, modifier = Modifier\.size\(24\.dp\)\)\s*\}"
    match = re.search(pattern, content)
    if match:
        target = match.group(0)

replacement = """                        val (styleName, painter) = styles[index]
                        val isUnlocked = autoConfig.unlockedStyles.contains(styleName)
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
    print("Target not found with regex")
