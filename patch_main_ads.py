import re

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace the onUnlockPremiumStyle call in setContent
target_onunlock = """                        onUnlockPremiumStyle = { styleName, onUnlocked ->
                            adsManager.showRewardedAd(this@MainActivity) {
                                val currentConfig = com.noxscreen.app.automation.AutomationSettings(this@MainActivity).getConfig()
                                val newUnlocked = currentConfig.unlockedStyles + styleName
                                val newConfig = currentConfig.copy(floatingLockStyle = styleName, unlockedStyles = newUnlocked)
                                com.noxscreen.app.automation.AutomationSettings(this@MainActivity).updateConfig(newConfig)
                                onUnlocked()
                            }
                        }"""
replacement_onunlock = """                        onUnlockPremiumStyle = { styleName, onLoading, onSuccess, onFailed ->
                            adsManager.showRewardedAdWithWait(
                                this@MainActivity,
                                onLoading = onLoading,
                                onSuccess = {
                                    val currentConfig = com.noxscreen.app.automation.AutomationSettings(this@MainActivity).getConfig()
                                    val newUnlocked = currentConfig.unlockedStyles + styleName
                                    val newConfig = currentConfig.copy(floatingLockStyle = styleName, unlockedStyles = newUnlocked)
                                    com.noxscreen.app.automation.AutomationSettings(this@MainActivity).updateConfig(newConfig)
                                    onSuccess()
                                },
                                onFailed = onFailed
                            )
                        }"""

content = content.replace(target_onunlock, replacement_onunlock)

# Add showAdLoading state to ZenithApp
target_zenith_sig = """@Composable
fun ZenithApp(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    isServiceRunning: Boolean,
    totalTimeSaved: Long,
    usageCount: Int,
    onUnlockPremiumStyle: (String, () -> Unit) -> Unit
) {"""
replacement_zenith_sig = """@Composable
fun ZenithApp(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    isServiceRunning: Boolean,
    totalTimeSaved: Long,
    usageCount: Int,
    onUnlockPremiumStyle: (String, () -> Unit, () -> Unit, () -> Unit) -> Unit
) {"""

content = content.replace(target_zenith_sig, replacement_zenith_sig)

# Define showAdLoading in ZenithApp
target_zenith_vars = """    val scrollState = rememberScrollState()
    
    val estimatedMah = ((totalTimeSaved / (1000f * 60f * 60f)) * 200f).toInt()"""
replacement_zenith_vars = """    val scrollState = rememberScrollState()
    
    var showAdLoading by remember { mutableStateOf(false) }
    
    val estimatedMah = ((totalTimeSaved / (1000f * 60f * 60f)) * 200f).toInt()"""

content = content.replace(target_zenith_vars, replacement_zenith_vars)

# Put showAdLoading at the end of ZenithApp's Box
target_end_box = """            }
        }
    }
}

@Composable
fun StatCard("""
replacement_end_box = """            }
        }
        
        if (showAdLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ZenithAccent)
            }
        }
    }
}

@Composable
fun StatCard("""

content = content.replace(target_end_box, replacement_end_box)

# Update onUnlockPremiumStyle invocation in the grid
target_invocation = """                                        onUnlockPremiumStyle(styleName) {
                                            autoConfig = automationSettings.getConfig()
                                        }"""
replacement_invocation = """                                        onUnlockPremiumStyle(
                                            styleName,
                                            { showAdLoading = true },
                                            { 
                                                showAdLoading = false
                                                autoConfig = automationSettings.getConfig()
                                            },
                                            { 
                                                showAdLoading = false
                                                android.widget.Toast.makeText(context, "Please connect to the internet to unlock this style.", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        )"""
content = content.replace(target_invocation, replacement_invocation)

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w', encoding='utf-8') as f:
    f.write(content)
