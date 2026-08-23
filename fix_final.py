with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

# We need to replace the broken `createNotificationChannel` with the FULL set of missing functions.

broken_block = """    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel("nox_screen_channel", "NoxScreen", android.app.NotificationManager.IMPORTANCE_LOW)
    private var errorLockIcon: ImageView? = null"""

fixed_block = """    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel("nox_screen_channel", "NoxScreen", android.app.NotificationManager.IMPORTANCE_LOW)
            getSystemService(android.app.NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createNotification(): android.app.Notification {
        val pendingIntent = android.app.PendingIntent.getActivity(this, 0, android.content.Intent(this, MainActivity::class.java), android.app.PendingIntent.FLAG_IMMUTABLE)
        return androidx.core.app.NotificationCompat.Builder(this, "nox_screen_channel")
            .setContentTitle("NoxScreen Active")
            .setContentText("Focus mode and smart automation are running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateAodInfo() {
        if (aodContainer?.visibility == android.view.View.VISIBLE) {
            val config = smartAutomationManager.settings.getConfig()
            val sdf = java.text.SimpleDateFormat(if (config.use24HourTime) "HH:mm" else "hh:mm a", java.util.Locale.getDefault())
            aodClockTextView?.text = sdf.format(java.util.Date())
            aodDateTextView?.text = java.text.SimpleDateFormat("EEE, MMM dd", java.util.Locale.getDefault()).format(java.util.Date())
        }
    }

    private fun updateFloatingBubbleStyle() {
        val config = smartAutomationManager.settings.getConfig()
        val resId = resources.getIdentifier("style_${config.floatingLockStyle}", "drawable", packageName)
        if (resId != 0) floatingIconView?.setImageResource(resId)
        val params = floatingIconView?.layoutParams
        if (params != null) {
            params.width = (100 * config.floatingLockSize).toInt()
            params.height = (100 * config.floatingLockSize).toInt()
            floatingIconView?.layoutParams = params
        }
    }

    private fun showFloatingBubbleInternal() {
        try {
            if (blackoutView?.parent != null) windowManager.removeView(blackoutView)
            if (floatingView?.parent == null) {
                updateFloatingBubbleStyle()
                windowManager.addView(floatingView, floatingLayoutParams)
            }
        } catch (e: Exception) {}
    }

    private fun showBlackoutInternal(showUnlockPageImmediately: Boolean = false) {
        try {
            if (floatingView?.parent != null) windowManager.removeView(floatingView)
            if (blackoutView?.parent == null) windowManager.addView(blackoutView, blackoutLayoutParams)
            
            if (showUnlockPageImmediately) {
                isUnlockScreenVisible = true
                tapCount = 0
                aodContainer?.visibility = android.view.View.GONE
                unlockButton?.visibility = android.view.View.VISIBLE
            } else {
                isUnlockScreenVisible = false
                tapCount = 0
                val config = smartAutomationManager.settings.getConfig()
                aodContainer?.visibility = if (config.isAodEnabled) android.view.View.VISIBLE else android.view.View.GONE
                unlockButton?.visibility = android.view.View.GONE
                updateAodInfo()
            }
        } catch (e: Exception) {
            try {
                val intent = android.content.Intent(this, BlackoutActivity::class.java)
                intent.putExtra("showUnlockPageImmediately", showUnlockPageImmediately)
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } catch (e2: Exception) {}
        }
    }

    private fun setupFloatingView() {
        floatingView = android.view.LayoutInflater.from(this).inflate(R.layout.layout_floating_icon, null)
        floatingIconView = floatingView?.findViewById(R.id.floating_icon) as? ImageView
        floatingView?.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                showBlackoutInternal(true)
            }
            true
        }
    }

    private var errorLockIcon: ImageView? = null"""

content = content.replace(broken_block, fixed_block)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
