import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target1 = """fun ZenithApp(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    isServiceRunning: Boolean,
    totalTimeSaved: Long,
    usageCount: Int
) {"""

replacement1 = """fun ZenithApp(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    isServiceRunning: Boolean,
    totalTimeSaved: Long,
    usageCount: Int,
    onUnlockPremiumStyle: (String) -> Unit
) {"""

target2 = """                        totalTimeSaved = totalTimeSaved,
                        usageCount = usageCount
                    )
                    }"""

replacement2 = """                        totalTimeSaved = totalTimeSaved,
                        usageCount = usageCount,
                        onUnlockPremiumStyle = { styleName ->
                            adsManager.showRewardedAd(this@MainActivity) {
                                val currentConfig = com.noxscreen.app.automation.AutomationSettings(this@MainActivity).getConfig()
                                val newUnlocked = currentConfig.unlockedStyles + styleName
                                val newConfig = currentConfig.copy(floatingLockStyle = styleName, unlockedStyles = newUnlocked)
                                com.noxscreen.app.automation.AutomationSettings(this@MainActivity).updateConfig(newConfig)
                            }
                        }
                    )
                    }"""

if target1 in content and target2 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Replaced ZenithApp params!")
else:
    print("Not found")
