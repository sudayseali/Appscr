import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target1 = """    onUnlockPremiumStyle: (String) -> Unit
) {"""

replacement1 = """    onUnlockPremiumStyle: (String, () -> Unit) -> Unit
) {"""

target2 = """                        onUnlockPremiumStyle = { styleName ->
                            adsManager.showRewardedAd(this@MainActivity) {
                                val currentConfig = com.noxscreen.app.automation.AutomationSettings(this@MainActivity).getConfig()
                                val newUnlocked = currentConfig.unlockedStyles + styleName
                                val newConfig = currentConfig.copy(floatingLockStyle = styleName, unlockedStyles = newUnlocked)
                                com.noxscreen.app.automation.AutomationSettings(this@MainActivity).updateConfig(newConfig)
                            }
                        }"""

replacement2 = """                        onUnlockPremiumStyle = { styleName, onUnlocked ->
                            adsManager.showRewardedAd(this@MainActivity) {
                                val currentConfig = com.noxscreen.app.automation.AutomationSettings(this@MainActivity).getConfig()
                                val newUnlocked = currentConfig.unlockedStyles + styleName
                                val newConfig = currentConfig.copy(floatingLockStyle = styleName, unlockedStyles = newUnlocked)
                                com.noxscreen.app.automation.AutomationSettings(this@MainActivity).updateConfig(newConfig)
                                onUnlocked()
                            }
                        }"""

target3 = """                                    } else {
                                        onUnlockPremiumStyle(styleName)
                                    }"""

replacement3 = """                                    } else {
                                        onUnlockPremiumStyle(styleName) {
                                            autoConfig = automationSettings.getConfig()
                                        }
                                    }"""

if target1 in content and target2 in content and target3 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    content = content.replace(target3, replacement3)
    with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Replaced callbacks!")
else:
    print("Not found")
