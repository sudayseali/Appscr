with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "r", encoding="utf-8") as f:
    content = f.read()

target_ontouch = """                            isUnlockScreenVisible = true
                            tapCount = 0
                            
                            if (config.isAodEnabled) {
                                aodContainer?.visibility = View.VISIBLE
                                updateAodInfo()
                                handler.post(timeUpdater)
                            } else {
                                aodContainer?.visibility = View.GONE
                                handler.removeCallbacks(timeUpdater)
                            }
                            unlockButton?.visibility = View.VISIBLE"""

replacement_ontouch = """                            isUnlockScreenVisible = true
                            tapCount = 0
                            
                            aodContainer?.visibility = View.VISIBLE
                            updateAodInfo()
                            handler.post(timeUpdater)
                            
                            unlockButton?.visibility = View.VISIBLE"""

target_timeupdater = """    private val timeUpdater = object : Runnable {
        override fun run() {
            val config = smartAutomationManager.settings.getConfig()
            if (config.isAodEnabled) {
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

if target_ontouch in content:
    content = content.replace(target_ontouch, replacement_ontouch)
else:
    print("target_ontouch not found")

if target_timeupdater in content:
    content = content.replace(target_timeupdater, replacement_timeupdater)
else:
    print("target_timeupdater not found")

with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "w", encoding="utf-8") as f:
    f.write(content)
