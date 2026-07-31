import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

# Add new fields
target_fields = """    private var aodStatusTextView: TextView? = null"""
replacement_fields = """    private var aodStatusTextView: TextView? = null
    private var aodDateTextView: TextView? = null
    private var aodContainer: View? = null
    private var unlockButton: View? = null
    private var tapCount = 0
    private var lastTapTime = 0L
    private var isUnlockScreenVisible = false
    private val handler = Handler(Looper.getMainLooper())
    private val timeUpdater = object : Runnable {
        override fun run() {
            if (isUnlockScreenVisible) {
                updateAodInfo()
                handler.postDelayed(this, 1000)
            }
        }
    }"""
content = content.replace(target_fields, replacement_fields)

# Add updateAodInfo method
target_update_aod = """    private fun getCurrentFormattedTime(): String {"""
replacement_update_aod = """    private fun updateAodInfo() {
        val timeSdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val dateSdf = java.text.SimpleDateFormat("EEE, MMM d", java.util.Locale.getDefault())
        val now = java.util.Date()
        aodClockTextView?.text = timeSdf.format(now)
        aodDateTextView?.text = dateSdf.format(now)
        aodBatteryTextView?.text = "🔋 ${getBatteryPercentage()}%"
    }

    private fun getCurrentFormattedTime(): String {"""
content = content.replace(target_update_aod, replacement_update_aod)

# Rewrite setupBlackoutView
target_setup_start = """    @SuppressLint("ClickableViewAccessibility")
    private fun setupBlackoutView() {"""
target_setup_end = """        blackoutView?.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }"""

setup_start_idx = content.find(target_setup_start)
setup_end_idx = content.find(target_setup_end) + len(target_setup_end)

new_setup_blackout = """    @SuppressLint("ClickableViewAccessibility")
    private fun setupBlackoutView() {
        blackoutView = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            
            // Top AOD Container
            val topContainer = LinearLayout(this@BlackScreenService).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(0, 200, 0, 0)
                visibility = View.GONE
            }
            aodContainer = topContainer

            aodClockTextView = TextView(this@BlackScreenService).apply {
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                textSize = 64f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            topContainer.addView(aodClockTextView)

            aodDateTextView = TextView(this@BlackScreenService).apply {
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                textSize = 16f
                setPadding(0, 16, 0, 32)
            }
            topContainer.addView(aodDateTextView)

            aodBatteryTextView = TextView(this@BlackScreenService).apply {
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                textSize = 16f
            }
            topContainer.addView(aodBatteryTextView)

            addView(topContainer, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, 
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            })
            
            // Bottom UNLOCK button
            unlockButton = TextView(this@BlackScreenService).apply {
                text = "UNLOCK"
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                textSize = 14f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                letterSpacing = 0.1f
                setPadding(0, 40, 0, 40)
                visibility = View.GONE
                
                setOnClickListener {
                    smartAutomationManager.handleManualDismiss()
                    showFloatingBubbleInternal()
                }
            }
            
            addView(unlockButton, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, 
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 150
            })
        }

        blackoutView?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (!isUnlockScreenVisible) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTapTime > 1500) {
                        tapCount = 0
                    }
                    lastTapTime = currentTime
                    tapCount++
                    
                    val config = smartAutomationManager.settings.getConfig()
                    if (tapCount >= config.tapsToWake) {
                        isUnlockScreenVisible = true
                        tapCount = 0
                        
                        aodContainer?.visibility = View.VISIBLE
                        unlockButton?.visibility = View.VISIBLE
                        updateAodInfo()
                        handler.post(timeUpdater)
                    }
                }
            }
            true
        }
    }"""

content = content[:setup_start_idx] + new_setup_blackout + content[setup_end_idx:]

# Rewrite showBlackoutInternal partially
target_show_blackout = """                if (config.isAodEnabled) {
                    aodClockTextView?.text = getCurrentFormattedTime()
                    aodClockTextView?.visibility = View.VISIBLE
                    aodBatteryTextView?.text = "🔋 ${getBatteryPercentage()}% Battery"
                    aodBatteryTextView?.visibility = View.VISIBLE
                    aodStatusTextView?.visibility = View.VISIBLE
                } else {
                    aodClockTextView?.visibility = View.GONE
                    aodBatteryTextView?.visibility = View.GONE
                    aodStatusTextView?.visibility = View.GONE
                }"""
replacement_show_blackout = """                // Always start pure black
                isUnlockScreenVisible = false
                tapCount = 0
                aodContainer?.visibility = View.GONE
                unlockButton?.visibility = View.GONE
                handler.removeCallbacks(timeUpdater)"""
content = content.replace(target_show_blackout, replacement_show_blackout)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
