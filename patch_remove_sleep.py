import re
import os

# Delete SleepTimerHandler.kt
try:
    os.remove("app/src/main/java/com/noxscreen/app/automation/SleepTimerHandler.kt")
except:
    pass

# SmartAutomationManager.kt
with open("app/src/main/java/com/noxscreen/app/automation/SmartAutomationManager.kt", "r", encoding="utf-8") as f:
    content = f.read()

content = re.sub(r'    var onSleepTimerTick: \(\(remainingSec: Long\) -> Unit\)\? = null,\n', '', content)
content = re.sub(r'    var onSleepTimerExpired: \(\(\) -> Unit\)\? = null\n', '', content)
content = re.sub(r'    val sleepTimerHandler = SleepTimerHandler\(\)\n', '', content)
content = re.sub(r' *stopSleepTimer\(\)\n', '', content)

target_sleep_start = """    fun startSleepTimerIfEnabled() {
        val config = settings.getConfig()
        if (config.isSleepTimerEnabled && config.sleepTimerDurationMinutes > 0) {
            sleepTimerHandler.startSleepTimer(
                durationMinutes = config.sleepTimerDurationMinutes,
                onTick = { remainingSec ->
                    onSleepTimerTick?.invoke(remainingSec)
                },
                onFinish = {
                    onSleepTimerExpired?.invoke()
                }
            )
        }
    }"""
content = content.replace(target_sleep_start, "")

target_sleep_stop = """    fun stopSleepTimer() {
        sleepTimerHandler.stopSleepTimer()
    }"""
content = content.replace(target_sleep_stop, "")

with open("app/src/main/java/com/noxscreen/app/automation/SmartAutomationManager.kt", "w", encoding="utf-8") as f:
    f.write(content)


# BlackScreenService.kt
with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "r", encoding="utf-8") as f:
    content = f.read()

target_sleep_init1 = """            onSleepTimerTick = { remainingSec ->
                val min = remainingSec / 60
                val sec = remainingSec % 60
                sleepTimerText?.text = String.format("%02d:%02d", min, sec)
                if (sleepTimerText?.visibility != View.VISIBLE) {
                    sleepTimerText?.visibility = View.VISIBLE
                }
            },"""
content = content.replace(target_sleep_init1, "")

target_sleep_init2 = """            onSleepTimerExpired = {
                // Dim screen entirely and act like we locked it natively.
                // For a more integrated approach, device admin permission would be needed to actually lock.
                // We'll just hide everything except a completely black screen and reduce brightness.
                aodContainer?.visibility = View.GONE
                sleepTimerText?.visibility = View.GONE
                unlockButton?.visibility = View.GONE
                floatingView?.visibility = View.GONE
                val currentParams = blackoutView?.layoutParams as? WindowManager.LayoutParams
                currentParams?.screenBrightness = 0f
                windowManager.updateViewLayout(blackoutView, currentParams)
            }"""
content = content.replace(target_sleep_init2, "")

# if there's any stray stopSleepTimer or startSleepTimerIfEnabled
content = re.sub(r' *smartAutomationManager\.stopSleepTimer\(\)\n', '', content)
content = re.sub(r' *smartAutomationManager\.startSleepTimerIfEnabled\(\)\n', '', content)

with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "w", encoding="utf-8") as f:
    f.write(content)


# MainActivity.kt
with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "r", encoding="utf-8") as f:
    content = f.read()

target_ui = """                ZenithSwitchRow(stringResource(R.string.sleep_timer), "Turn off screen completely after time", autoConfig.isSleepTimerEnabled) {
                    autoConfig = autoConfig.copy(isSleepTimerEnabled = it)
                    automationSettings.updateConfig(autoConfig)
                }
                if (autoConfig.isSleepTimerEnabled) {
                    Text("Time to sleep: ${autoConfig.sleepTimerDurationMinutes} minutes", color = ZenithSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                    Slider(
                        value = autoConfig.sleepTimerDurationMinutes.toFloat(),
                        onValueChange = { 
                            autoConfig = autoConfig.copy(sleepTimerDurationMinutes = it.toInt())
                            automationSettings.updateConfig(autoConfig)
                        },
                        valueRange = 1f..120f,
                        steps = 119
                    )
                }"""
content = content.replace(target_ui, "")

# A second way to find it in case formatting differs
content = re.sub(r'\s*ZenithSwitchRow\(stringResource\(R\.string\.sleep_timer\).*?steps = 119\s*\)', '', content, flags=re.DOTALL)

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "w", encoding="utf-8") as f:
    f.write(content)

