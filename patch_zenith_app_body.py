import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """                                .clickable { 
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
                                },"""

replacement = """                                .clickable { 
                                    if (isUnlocked) {
                                        autoConfig = autoConfig.copy(floatingLockStyle = styleName)
                                        automationSettings.updateConfig(autoConfig)
                                    } else {
                                        onUnlockPremiumStyle(styleName)
                                    }
                                },"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Replaced ZenithApp body!")
else:
    print("Not found")
