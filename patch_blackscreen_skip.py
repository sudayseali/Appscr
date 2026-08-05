with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "r", encoding="utf-8") as f:
    content = f.read()

target = """                    if (tapCount >= config.tapsToWake) {
                        isUnlockScreenVisible = true
                        tapCount = 0
                        
                        aodContainer?.visibility = View.VISIBLE
                        unlockButton?.visibility = View.VISIBLE
                        updateAodInfo()
                        handler.post(timeUpdater)
                        
                        handler.removeCallbacks(resetToBlackRunnable)
                        handler.postDelayed(resetToBlackRunnable, 10000)
                    }"""

replacement = """                    if (tapCount >= config.tapsToWake) {
                        if (config.isSkipUnlockScreenEnabled) {
                            smartAutomationManager.handleManualDismiss()
                            showFloatingBubbleInternal()
                        } else {
                            isUnlockScreenVisible = true
                            tapCount = 0
                            
                            aodContainer?.visibility = View.VISIBLE
                            unlockButton?.visibility = View.VISIBLE
                            updateAodInfo()
                            handler.post(timeUpdater)
                            
                            handler.removeCallbacks(resetToBlackRunnable)
                            handler.postDelayed(resetToBlackRunnable, 10000)
                        }
                    }"""

if 'if (config.isSkipUnlockScreenEnabled)' not in content:
    content = content.replace(target, replacement)

with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "w", encoding="utf-8") as f:
    f.write(content)
