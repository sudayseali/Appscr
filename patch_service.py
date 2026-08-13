with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target1 = """        usageLimitMonitor = com.noxscreen.app.automation.UsageLimitMonitor(
            context = this,
            automationSettings = smartAutomationManager.settings,
            onTriggerBlock = {
                showBlackoutInternal()
            }
        )"""

replacement1 = """        usageLimitMonitor = com.noxscreen.app.automation.UsageLimitMonitor(
            context = this,
            automationSettings = smartAutomationManager.settings,
            onTriggerBlock = {
                showBlackoutInternal(showUnlockPageImmediately = true)
            }
        )"""

target2 = """    private fun showBlackoutInternal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            return
        }
        try {
            if (floatingView?.parent != null) {
                windowManager.removeView(floatingView)
            }
        } catch (e: Exception) { }

        try {
            if (blackoutView?.parent == null) {
                val config = smartAutomationManager.settings.getConfig()
                blackoutView?.setBackgroundColor(Color.BLACK)

                // Always start pure black
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

replacement2 = """    private fun showBlackoutInternal(showUnlockPageImmediately: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            return
        }
        try {
            if (floatingView?.parent != null) {
                windowManager.removeView(floatingView)
            }
        } catch (e: Exception) { }

        try {
            if (blackoutView?.parent == null) {
                val config = smartAutomationManager.settings.getConfig()
                blackoutView?.setBackgroundColor(Color.BLACK)

                if (showUnlockPageImmediately) {
                    isUnlockScreenVisible = true
                    tapCount = 0
                    aodContainer?.visibility = View.GONE
                    handler.removeCallbacks(timeUpdater)
                    unlockButton?.visibility = View.VISIBLE
                    
                    handler.removeCallbacks(resetToBlackRunnable)
                    handler.postDelayed(resetToBlackRunnable, 10000)
                } else {
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
                    handler.removeCallbacks(resetToBlackRunnable)
                }"""

if target1 in content and target2 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
        f.write(content)
    print("Patched BlackScreenService")
else:
    print("Targets not found")
