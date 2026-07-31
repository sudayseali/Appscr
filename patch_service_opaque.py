import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target_params = """                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
                )"""

replacement_params = """                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.OPAQUE
                )"""

content = content.replace(target_params, replacement_params)

target_fields = """    private val timeUpdater = object : Runnable {
        override fun run() {
            if (isUnlockScreenVisible) {
                updateAodInfo()
                handler.postDelayed(this, 1000)
            }
        }
    }"""
replacement_fields = """    private val timeUpdater = object : Runnable {
        override fun run() {
            if (isUnlockScreenVisible) {
                updateAodInfo()
                handler.postDelayed(this, 1000)
            }
        }
    }
    private val resetToBlackRunnable = Runnable {
        isUnlockScreenVisible = false
        tapCount = 0
        aodContainer?.visibility = View.GONE
        unlockButton?.visibility = View.GONE
        handler.removeCallbacks(timeUpdater)
    }"""
content = content.replace(target_fields, replacement_fields)

target_tap = """                        aodContainer?.visibility = View.VISIBLE
                        unlockButton?.visibility = View.VISIBLE
                        updateAodInfo()
                        handler.post(timeUpdater)
                    }
                }
            }"""
replacement_tap = """                        aodContainer?.visibility = View.VISIBLE
                        unlockButton?.visibility = View.VISIBLE
                        updateAodInfo()
                        handler.post(timeUpdater)
                        
                        handler.removeCallbacks(resetToBlackRunnable)
                        handler.postDelayed(resetToBlackRunnable, 10000)
                    }
                }
            }"""
content = content.replace(target_tap, replacement_tap)

target_unlock = """                setOnClickListener {
                    smartAutomationManager.handleManualDismiss()
                    showFloatingBubbleInternal()
                }"""
replacement_unlock = """                setOnClickListener {
                    handler.removeCallbacks(resetToBlackRunnable)
                    smartAutomationManager.handleManualDismiss()
                    showFloatingBubbleInternal()
                }"""
content = content.replace(target_unlock, replacement_unlock)

target_fallback = """                // Always start pure black
                isUnlockScreenVisible = false
                tapCount = 0
                aodContainer?.visibility = View.GONE
                unlockButton?.visibility = View.GONE
                handler.removeCallbacks(timeUpdater)"""
replacement_fallback = """                // Always start pure black
                isUnlockScreenVisible = false
                tapCount = 0
                aodContainer?.visibility = View.GONE
                unlockButton?.visibility = View.GONE
                handler.removeCallbacks(timeUpdater)
                handler.removeCallbacks(resetToBlackRunnable)"""
content = content.replace(target_fallback, replacement_fallback)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
