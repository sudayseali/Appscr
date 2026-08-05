with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "r", encoding="utf-8") as f:
    content = f.read()

target_timeupdater = """    private val timeUpdater = object : Runnable {
        override fun run() {
            if (isUnlockScreenVisible) {
                updateAodInfo()
                handler.postDelayed(this, 1000)
            }
        }
    }"""

replacement_timeupdater = """    private val timeUpdater = object : Runnable {
        override fun run() {
            val config = smartAutomationManager.settings.getConfig()
            if (isUnlockScreenVisible || config.isAodEnabled) {
                updateAodInfo()
                handler.postDelayed(this, 1000)
            }
        }
    }"""

target_reset = """    private val resetToBlackRunnable = Runnable {
        isUnlockScreenVisible = false
        tapCount = 0
        aodContainer?.visibility = View.GONE
        unlockButton?.visibility = View.GONE
        handler.removeCallbacks(timeUpdater)
    }"""

replacement_reset = """    private val resetToBlackRunnable = Runnable {
        isUnlockScreenVisible = false
        tapCount = 0
        val config = smartAutomationManager.settings.getConfig()
        if (config.isAodEnabled) {
            aodContainer?.visibility = View.VISIBLE
            updateAodInfo()
            handler.post(timeUpdater)
        } else {
            aodContainer?.visibility = View.GONE
            handler.removeCallbacks(timeUpdater)
        }
        unlockButton?.visibility = View.GONE
    }"""

target_showblackout = """                // Always start pure black
                isUnlockScreenVisible = false
                tapCount = 0
                aodContainer?.visibility = View.GONE
                unlockButton?.visibility = View.GONE
                handler.removeCallbacks(timeUpdater)
                handler.removeCallbacks(resetToBlackRunnable)"""

replacement_showblackout = """                // Always start pure black
                isUnlockScreenVisible = false
                tapCount = 0
                if (config.isAodEnabled) {
                    aodContainer?.visibility = View.VISIBLE
                    updateAodInfo()
                    handler.post(timeUpdater)
                } else {
                    aodContainer?.visibility = View.GONE
                    handler.removeCallbacks(timeUpdater)
                }
                unlockButton?.visibility = View.GONE
                handler.removeCallbacks(resetToBlackRunnable)"""

target_ontouch = """                            aodContainer?.visibility = View.VISIBLE
                            unlockButton?.visibility = View.VISIBLE
                            updateAodInfo()
                            handler.post(timeUpdater)"""

replacement_ontouch = """                            // AOD is hidden on unlock screen as requested by user
                            aodContainer?.visibility = View.GONE
                            unlockButton?.visibility = View.VISIBLE"""

if target_timeupdater in content:
    content = content.replace(target_timeupdater, replacement_timeupdater)
else:
    print("timeupdater target not found")

if target_reset in content:
    content = content.replace(target_reset, replacement_reset)
else:
    print("reset target not found")

if target_showblackout in content:
    content = content.replace(target_showblackout, replacement_showblackout)
else:
    print("showblackout target not found")

if target_ontouch in content:
    content = content.replace(target_ontouch, replacement_ontouch)
else:
    print("ontouch target not found")

with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "w", encoding="utf-8") as f:
    f.write(content)
