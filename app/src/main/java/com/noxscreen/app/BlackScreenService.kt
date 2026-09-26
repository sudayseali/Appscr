package com.noxscreen.app

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import kotlin.math.abs

class BlackScreenService : Service() {
    companion object {
        var isRunning = false
            private set
            
        fun updateTile(context: android.content.Context) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                android.service.quicksettings.TileService.requestListeningState(
                    context, 
                    android.content.ComponentName(context, NoxTileService::class.java)
                )
            }
            // Update widget as well
            NoxWidgetProvider.updateAllWidgets(context)
        }
    }


    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var blackoutView: View? = null
    private var sleepTimerTextView: TextView? = null
    private var aodClockTextView: TextView? = null
    private var aodBatteryTextView: TextView? = null
    private var aodStatusTextView: TextView? = null
    private var aodDateTextView: TextView? = null
    private var aodContainer: View? = null
    private var unlockButton: View? = null
    private var tapCount = 0
    private var lastTapTime = 0L
    private var isUnlockScreenVisible = false
    private val handler = Handler(Looper.getMainLooper())
    private val timeUpdater = object : Runnable {
        override fun run() {
            val config = smartAutomationManager.settings.getConfig()
            if (isUnlockScreenVisible || config.isAodEnabled) {
                updateAodInfo()
                handler.postDelayed(this, 1000)
            }
        }
    }
    private val resetToBlackRunnable = Runnable {
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
    }
    
    private var blackoutStartTime = 0L

    private val channelId = "BlackScreenChannel"
    private val notificationId = 1

    private lateinit var smartAutomationManager: com.noxscreen.app.automation.SmartAutomationManager
    private lateinit var usageLimitMonitor: com.noxscreen.app.automation.UsageLimitMonitor

    override fun onBind(intent: Intent?): IBinder? = null

    private val settingsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.noxscreen.app.SETTINGS_UPDATED") {
                updateFloatingBubbleStyle()
                applyUnlockButtonStyle()
                if (::smartAutomationManager.isInitialized) {
                    smartAutomationManager.stopSensors()
                    smartAutomationManager.startSensors()
                }
                if (::usageLimitMonitor.isInitialized) {
                    usageLimitMonitor.syncWithConfig()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        
        val filter = IntentFilter("com.noxscreen.app.SETTINGS_UPDATED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(settingsReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(settingsReceiver, filter)
        }
        
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        smartAutomationManager = com.noxscreen.app.automation.SmartAutomationManager(
            context = this,
            onTriggerOverlay = { _ ->
                showBlackoutInternal()
            },
            onRemoveOverlay = {
                showFloatingBubbleInternal()
            }
        )

        usageLimitMonitor = com.noxscreen.app.automation.UsageLimitMonitor(
            context = this,
            automationSettings = smartAutomationManager.settings,
            isOverlayCurrentlyActive = { blackoutView?.parent != null },
            onTriggerBlock = {
                showBlackoutInternal()
            }
        )
        
        createNotificationChannel()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                }
                startForeground(notificationId, createNotification(), serviceType)
            } else {
                startForeground(notificationId, createNotification())
            }
        } catch (e: Exception) {
            Log.e("BlackScreenService", "Error starting foreground service: ${e.message}")
        }
        
        setupFloatingView()
        setupBlackoutView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            isRunning = false
            updateTile(this)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            stopSelf()
            return START_NOT_STICKY
        }
        isRunning = true
        updateTile(this)
        
        if (intent?.action == "BIOMETRIC_SUCCESS") {
            smartAutomationManager.handleManualDismiss()
            showFloatingBubbleInternal()
            return START_STICKY
        }
        
        if (intent?.action == "START_BLACKOUT") {
            smartAutomationManager.handleUserActivation()
            smartAutomationManager.startSensors()
        } else if (intent?.action == "BIOMETRIC_FAILED") {
            blackoutView?.visibility = View.VISIBLE
            isUnlockScreenVisible = false
            aodContainer?.visibility = View.GONE
            unlockButton?.visibility = View.GONE
            
            // Re-hide the screen after 10 seconds if user doesn't unlock
            handler.removeCallbacks(resetToBlackRunnable)
            handler.postDelayed(resetToBlackRunnable, 10000)
            
            return START_STICKY
        } else {
            showFloatingBubbleInternal()
            smartAutomationManager.startSensors()
        }
        usageLimitMonitor.syncWithConfig()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "NoxScreen Pro Service",
                NotificationManager.IMPORTANCE_LOW
        )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val stopIntent = Intent(this, BlackScreenService::class.java).apply {
            action = "STOP_SERVICE"
        }
        val pendingStopIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("NoxScreen Pro Active")
            .setContentText("Tap to stop")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentIntent(pendingStopIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private var floatingIconView: ImageView? = null
    private var floatingLayoutParams: WindowManager.LayoutParams? = null

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFloatingView() {
        floatingView = FrameLayout(this).apply {
            val icon = ImageView(this@BlackScreenService).apply {
                setImageResource(R.drawable.ic_moon)
                setBackgroundResource(R.drawable.floating_icon_bg)
                setColorFilter(android.graphics.Color.parseColor("#FFC107"))
                setPadding(32, 32, 32, 32)
                elevation = 16f
            }
            floatingIconView = icon
            addView(icon, FrameLayout.LayoutParams(160, 160))
        }

        floatingLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        floatingLayoutParams?.gravity = Gravity.TOP or Gravity.START
        floatingLayoutParams?.x = 0
        floatingLayoutParams?.y = 100

        val params = floatingLayoutParams!!

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false

        floatingView?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (abs(dx) > 10 || abs(dy) > 10) {
                        isClick = false
                    }
                    params.x = initialX + dx
                    params.y = initialY + dy
                    windowManager.updateViewLayout(floatingView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        smartAutomationManager.handleUserActivation()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun getBatteryPercentage(): Int {
        return try {
            val bm = getSystemService(Context.BATTERY_SERVICE) as? android.os.BatteryManager
            bm?.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
        } catch (e: Exception) {
            100
        }
    }

    private fun dpToPx(dp: Float): Int {
        return (dp * resources.displayMetrics.density).toInt().coerceAtLeast(1)
    }

    private fun updateAodInfo() {
        val config = smartAutomationManager.settings.getConfig()
        val timePattern = if (config.use24HourTime) "HH:mm" else "hh:mm a"
        val timeSdf = java.text.SimpleDateFormat(timePattern, java.util.Locale.getDefault())
        val dateSdf = java.text.SimpleDateFormat("EEE, MMM d", java.util.Locale.getDefault())
        val now = java.util.Date()
        
        val themeColor = when (config.aodThemeColor) {
            "green" -> android.graphics.Color.parseColor("#69F0AE")
            "blue" -> android.graphics.Color.parseColor("#82B1FF")
            "yellow" -> android.graphics.Color.parseColor("#FFD54F")
            "pink" -> android.graphics.Color.parseColor("#FF80AB")
            else -> android.graphics.Color.WHITE
        }

        when (config.clockStyle) {
            "huge" -> {
                aodClockTextView?.textSize = 110f
                aodClockTextView?.setTextColor(themeColor)
                aodClockTextView?.typeface = android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL)
            }
            "analog" -> {
                aodClockTextView?.textSize = 72f
                aodClockTextView?.setTextColor(themeColor)
                aodClockTextView?.typeface = android.graphics.Typeface.create("serif", android.graphics.Typeface.ITALIC)
            }
            "dino" -> {
                aodClockTextView?.textSize = 60f
                aodClockTextView?.setTextColor(themeColor)
                aodClockTextView?.typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
            }
            else -> {
                aodClockTextView?.textSize = 100f
                aodClockTextView?.setTextColor(themeColor)
                aodClockTextView?.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
            }
        }
        
        if (config.clockStyle == "dino") {
            aodClockTextView?.text = "🦖\n${timeSdf.format(now)}"
        } else if (config.clockStyle == "analog") {
            aodClockTextView?.text = "🕰️\n${timeSdf.format(now)}"
        } else {
            aodClockTextView?.text = timeSdf.format(now)
        }
        
        aodDateTextView?.text = dateSdf.format(now)
        aodDateTextView?.setTextColor(themeColor)
        
        if (config.showBatteryPercentage) {
            aodBatteryTextView?.visibility = View.VISIBLE
            aodBatteryTextView?.text = "🔋 ${getBatteryPercentage()}%"
            aodBatteryTextView?.setTextColor(themeColor)
        } else {
            aodBatteryTextView?.visibility = View.GONE
        }
        
        if (config.oledBurnInProtection) {
            val random = java.util.Random()
            val xOffset = random.nextInt(31) - 15 // -15 to +15 pixels
            val yOffset = random.nextInt(31) - 15
            aodContainer?.translationX = xOffset.toFloat()
            aodContainer?.translationY = yOffset.toFloat()
        } else {
            aodContainer?.translationX = 0f
            aodContainer?.translationY = 0f
        }
    }

    private fun getCurrentFormattedTime(): String {
        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun applyUnlockButtonStyle() {
        val button = unlockButton as? TextView ?: return
        val config = smartAutomationManager.settings.getConfig()
        val style = config.unlockScreenStyle

        button.setOnTouchListener(null)
        button.setOnClickListener(null)

        when (style) {
            "swipe" -> {
                button.text = "SWIPE UP TO UNLOCK"
                button.setTextColor(Color.WHITE)
                button.textSize = 16f
                button.background = null
                button.setPadding(0, dpToPx(16f), 0, dpToPx(16f))
                button.gravity = Gravity.CENTER

                var startY = 0f
                button.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            startY = event.y
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            val endY = event.y
                            if (startY - endY > dpToPx(36f)) { // Swiped up
                                handleBlackoutUnlock()
                            }
                            true
                        }
                        else -> false
                    }
                }
            }
            "icon" -> {
                button.text = "🔓"
                button.setTextColor(Color.WHITE)
                button.textSize = 40f
                button.background = null
                button.setPadding(dpToPx(16f), dpToPx(16f), dpToPx(16f), dpToPx(16f))
                button.gravity = Gravity.CENTER

                button.setOnClickListener {
                    handleBlackoutUnlock()
                }
            }
            else -> { // "button"
                button.text = "UNLOCK"
                button.setTextColor(Color.BLACK)
                val bg = GradientDrawable()
                bg.setColor(Color.WHITE)
                bg.cornerRadius = dpToPx(24f).toFloat()
                button.background = bg
                button.gravity = Gravity.CENTER
                button.textSize = 16f
                button.typeface = android.graphics.Typeface.DEFAULT_BOLD
                button.letterSpacing = 0.1f
                button.setPadding(dpToPx(32f), dpToPx(16f), dpToPx(32f), dpToPx(16f))

                button.setOnClickListener {
                    handleBlackoutUnlock()
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBlackoutView() {
        blackoutView = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            
            // Top AOD Container
            val topContainer = LinearLayout(this@BlackScreenService).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(0, dpToPx(80f), 0, 0)
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
                textSize = 18f
                setPadding(0, 0, 0, 16)
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
            
            unlockButton = TextView(this@BlackScreenService).apply {
                visibility = View.GONE
            }
            applyUnlockButtonStyle()
            
            addView(unlockButton, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, 
                FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = dpToPx(64f)
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
                        if (config.isSkipUnlockScreenEnabled) {
                            tapCount = 0
                            handleBlackoutUnlock()
                        } else {
                            isUnlockScreenVisible = true
                            tapCount = 0
                            applyUnlockButtonStyle()
                            
                            aodContainer?.visibility = View.VISIBLE
                            updateAodInfo()
                            handler.post(timeUpdater)
                            
                            unlockButton?.visibility = View.VISIBLE
                            
                            handler.removeCallbacks(resetToBlackRunnable)
                            handler.postDelayed(resetToBlackRunnable, 10000)
                        }
                    }
                }
            }
            true
        }
    }

    private fun updateFloatingBubbleStyle() {
        val entitlementManager = com.noxscreen.app.automation.FloatingLockEntitlementManager(this)
        val config = smartAutomationManager.settings.getConfig()
        val baseSizePx = dpToPx(64f)
        val basePaddingPx = dpToPx(10f)
        val size = (baseSizePx * config.floatingLockSize).toInt().coerceAtLeast(dpToPx(48f))
        val padding = (basePaddingPx * config.floatingLockSize).toInt().coerceAtLeast(dpToPx(6f))
        
        // Ensure that if the selected style expired, fallback to "lock"
        val activeStyle = if (entitlementManager.isStyleUnlocked(config.floatingLockStyle)) {
            config.floatingLockStyle
        } else {
            "lock"
        }
        
        floatingView?.visibility = if (config.hideFloatingButton) View.GONE else View.VISIBLE
        
        floatingIconView?.apply {
            layoutParams = FrameLayout.LayoutParams(size, size)
            setPadding(padding, padding, padding, padding)
            
            val iconRes = when (activeStyle) {
                "lock" -> R.drawable.ic_lock
                "moon" -> R.drawable.ic_moon
                "circle" -> R.drawable.ic_circle
                "double_circle" -> R.drawable.ic_double_circle
                "key" -> R.drawable.ic_key
                "eye_off" -> R.drawable.ic_eye_off
                "shield" -> R.drawable.ic_shield
                "fingerprint" -> R.drawable.ic_fingerprint
                "power" -> R.drawable.ic_power
                "bolt" -> R.drawable.ic_bolt
                "favorite" -> R.drawable.ic_favorite
                "crown" -> R.drawable.ic_crown
                "diamond" -> R.drawable.ic_diamond
                "star" -> R.drawable.ic_star
                "fire" -> R.drawable.ic_fire
                "atom" -> R.drawable.ic_atom
                "shield_lock" -> R.drawable.ic_shield_lock
                else -> R.drawable.ic_moon
            }
            setImageResource(iconRes)
            requestLayout()
        }
        try {
            if (floatingView?.parent != null) {
                windowManager.updateViewLayout(floatingView, floatingLayoutParams)
            }
        } catch (e: Exception) {}
    }

    private fun handleBlackoutUnlock() {
        handler.removeCallbacks(resetToBlackRunnable)

        // Always read the live real-time configuration directly from SharedPreferences
        val currentConfig = smartAutomationManager.settings.getConfig()
        val securityManager = com.noxscreen.app.security.AppSecurityManager(this)
        val isBiometricReady = securityManager.checkBiometricAvailability() == com.noxscreen.app.security.AppSecurityManager.BiometricStatus.AVAILABLE

        if (currentConfig.isBiometricEnabled) {
            if (isBiometricReady) {
                // Keep blackoutView pure solid black behind biometric dialog
                // so user never sees wallpaper, apps or any piece of the phone until authenticated
                aodContainer?.visibility = View.GONE
                unlockButton?.visibility = View.GONE
                val intent = Intent(this, BiometricAuthActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("AUTH_TARGET", "BLACKOUT")
                }
                startActivity(intent)
            } else {
                // Fingerprint/PIN was disabled or removed by user in Android Settings!
                // Clear the obsolete security config so app doesn't keep stale state
                smartAutomationManager.settings.updateConfig(currentConfig.copy(isBiometricEnabled = false, isAntiSpyEnabled = false))
                smartAutomationManager.handleManualDismiss()
                showFloatingBubbleInternal()
                android.widget.Toast.makeText(
                    this,
                    "Taleefanka lagama helin Fingerprint/PIN. Shaashadda waa la furay.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        } else {
            smartAutomationManager.handleManualDismiss()
            showFloatingBubbleInternal()
        }
    }

    private fun showFloatingBubbleInternal() {
        handler.removeCallbacks(timeUpdater)
        updateFloatingBubbleStyle()
        sleepTimerTextView?.visibility = View.GONE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            return
        }
        if (blackoutStartTime > 0) {
            addTimeSaved(System.currentTimeMillis() - blackoutStartTime)
            blackoutStartTime = 0
        }
        try {
            if (blackoutView?.parent != null) {
                windowManager.removeView(blackoutView)
            }
        } catch (e: Exception) { }

        try {
            if (floatingView?.parent == null) {
                windowManager.addView(floatingView, floatingLayoutParams)
            }
        } catch (e: Exception) {
            android.util.Log.e("BlackScreenService", "Error adding floating view", e)
        }
    }

    private fun showBlackoutInternal() {
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
                handler.removeCallbacks(resetToBlackRunnable)

                var windowFlags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                if (config.isAntiSpyEnabled) {
                    windowFlags = windowFlags or WindowManager.LayoutParams.FLAG_SECURE
                }

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
                    windowFlags,
                    PixelFormat.OPAQUE
        ).apply {
                    screenBrightness = 0f
                    buttonBrightness = 0f
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        preferredRefreshRate = 30f // Suggest lowest standard refresh rate
                    }
                }
                
                @Suppress("DEPRECATION")
                val uiFlags = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
                
                blackoutView?.systemUiVisibility = uiFlags

                windowManager.addView(blackoutView, params)
                
                blackoutView?.post {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        blackoutView?.windowInsetsController?.let { controller ->
                            controller.hide(android.view.WindowInsets.Type.systemBars())
                            controller.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        blackoutView?.systemUiVisibility = uiFlags
                    }
                }
                
                blackoutStartTime = System.currentTimeMillis()
                incrementUsageCount()
            }
        } catch (e: Exception) {
            // Fallback to Activity if overlay is denied by AppOps
            try {
                val intent = Intent(this, BlackoutActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        isRunning = false
        updateTile(this)
        super.onDestroy()
        usageLimitMonitor.stopMonitoring()
        try {
            unregisterReceiver(settingsReceiver)
        } catch (e: Exception) {}
        smartAutomationManager.stopSensors()
        if (blackoutStartTime > 0) {
            addTimeSaved(System.currentTimeMillis() - blackoutStartTime)
            blackoutStartTime = 0
        }
        try {
            if (floatingView?.parent != null) windowManager.removeView(floatingView)
            if (blackoutView?.parent != null) windowManager.removeView(blackoutView)
        } catch (e: Exception) { }
    }

    private fun addTimeSaved(durationInMillis: Long) {
        val prefs = getSharedPreferences("BlackScreenStats", Context.MODE_PRIVATE)
        val totalTime = prefs.getLong("total_time_saved", 0L)
        prefs.edit().putLong("total_time_saved", totalTime + durationInMillis).apply()
    }

    private fun incrementUsageCount() {
        val prefs = getSharedPreferences("BlackScreenStats", Context.MODE_PRIVATE)
        val count = prefs.getInt("usage_count", 0)
        prefs.edit().putInt("usage_count", count + 1).apply()
    }
}
