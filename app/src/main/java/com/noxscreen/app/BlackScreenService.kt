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
        private val _isRunningFlow = kotlinx.coroutines.flow.MutableStateFlow(false)
        val isRunningFlow: kotlinx.coroutines.flow.StateFlow<Boolean> = _isRunningFlow

        var isRunning = false
            private set(value) {
                field = value
                _isRunningFlow.value = value
            }
            
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
    private var floatingIconView: ImageView? = null
    private var floatingLayoutParams: WindowManager.LayoutParams? = null
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
                if (::smartAutomationManager.isInitialized) {
                    smartAutomationManager.stopSensors()
                    smartAutomationManager.startSensors()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        com.noxscreen.app.automation.FloatingLockEntitlementManager(this).validateActiveStyle()
        
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
                handleUnlockRequest()
            }
        )

        usageLimitMonitor = com.noxscreen.app.automation.UsageLimitMonitor(
            context = this,
            automationSettings = smartAutomationManager.settings,
            onTriggerBlock = {
                showBlackoutInternal(showUnlockPageImmediately = true)
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


    private fun stopSelfAndCleanUp() {
        isRunning = false
        updateTile(this)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "AUTH_SUCCESS_STOP") {
            stopSelfAndCleanUp()
            return START_NOT_STICKY
        }
        if (intent?.action == "STOP_SERVICE") {
            if (usageLimitMonitor.isCurrentlyBlocked) {
                return START_STICKY
            }
            val config = smartAutomationManager.settings.getConfig()
            if (config.isBiometricEnabled) {
                com.noxscreen.app.security.AuthenticationManager.setAuthenticating()
                val successIntent = android.app.PendingIntent.getService(
                    this, 1,
                    android.content.Intent(this, BlackScreenService::class.java).apply { action = "AUTH_SUCCESS_STOP" },
                    android.app.PendingIntent.FLAG_IMMUTABLE
                )
                val authIntent = android.content.Intent(this, BiometricAuthActivity::class.java).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("EXTRA_SUCCESS_INTENT", successIntent)
                }
                startActivity(authIntent)
                return START_STICKY
            } else {
                stopSelfAndCleanUp()
                return START_NOT_STICKY
            }
        }
        if (intent?.action == "AUTH_SUCCESS_UNLOCK") {
            smartAutomationManager.handleManualDismiss()
            showFloatingBubbleInternal()
            return START_STICKY
        }
        isRunning = true
        return START_STICKY
    }

    private fun handleUnlockRequest() {
        val config = smartAutomationManager.settings.getConfig()
        handler.removeCallbacks(resetToBlackRunnable)
        if (config.isBiometricEnabled) {
            com.noxscreen.app.security.AuthenticationManager.setAuthenticating()
            val successIntent = android.app.PendingIntent.getService(
                this, 2,
                android.content.Intent(this, BlackScreenService::class.java).apply { action = "AUTH_SUCCESS_UNLOCK" },
                android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val authIntent = android.content.Intent(this, BiometricAuthActivity::class.java).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("EXTRA_SUCCESS_INTENT", successIntent)
            }
            startActivity(authIntent)
        } else {
            smartAutomationManager.handleManualDismiss()
            showFloatingBubbleInternal()
        }
    }

    private fun createNotificationChannel() {
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

    private fun setupFloatingView() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }
        floatingLayoutParams = params

        val frameLayout = FrameLayout(this)
        floatingView = frameLayout

        val iconView = ImageView(this).apply {
            setBackgroundResource(R.drawable.floating_icon_bg)
            val config = smartAutomationManager.settings.getConfig()
            val size = (150 * config.floatingLockSize).toInt()
            val padding = (24 * config.floatingLockSize).toInt()
            layoutParams = FrameLayout.LayoutParams(size, size)
            setPadding(padding, padding, padding, padding)
            setImageResource(R.drawable.ic_moon)
        }
        floatingIconView = iconView
        frameLayout.addView(iconView)

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = true

        frameLayout.setOnTouchListener { v, event ->
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

                    val metrics = resources.displayMetrics
                    val maxX = metrics.widthPixels - v.width
                    val maxY = metrics.heightPixels - v.height

                    var newX = initialX + dx
                    var newY = initialY + dy

                    if (newX < 0) newX = 0
                    if (newX > maxX) newX = maxX
                    if (newY < 0) newY = 0
                    if (newY > maxY) newY = maxY

                    params.x = newX
                    params.y = newY

                    windowManager.updateViewLayout(floatingView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        showBlackoutInternal()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private var errorLockIcon: ImageView? = null

    private fun showErrorShakeAnimation() {
        errorLockIcon?.visibility = View.VISIBLE
        val shake = android.view.animation.TranslateAnimation(0f, 20f, 0f, 0f)
        shake.duration = 50
        shake.repeatMode = android.view.animation.Animation.REVERSE
        shake.repeatCount = 5
        shake.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                errorLockIcon?.visibility = View.GONE
            }
        })
        errorLockIcon?.startAnimation(shake)
    }

    @android.annotation.SuppressLint("ClickableViewAccessibility")
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
                val config = smartAutomationManager.settings.getConfig()
                val style = config.unlockScreenStyle
                
                when (style) {
                    "swipe" -> {
                        text = "SWIPE UP TO UNLOCK"
                        setTextColor(Color.WHITE)
                        background = null
                        setPadding(0, 40, 0, 40)
                        gravity = Gravity.CENTER
                        
                        var startY = 0f
                        setOnTouchListener { v, event ->
                            when (event.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    startY = event.y
                                    true
                                }
                                MotionEvent.ACTION_UP -> {
                                    val endY = event.y
                                    if (startY - endY > 100) { // Swiped up
                                        handleUnlockRequest()
                                    }
                                    true
                                }
                                else -> false
                            }
                        }
                    }
                    "icon" -> {
                        text = "🔓"
                        setTextColor(Color.WHITE)
                        textSize = 40f
                        background = null
                        setPadding(40, 40, 40, 40)
                        gravity = Gravity.CENTER
                        
                        setOnClickListener {
                            handleUnlockRequest()
                        }
                    }
                    else -> { // "button"
                        text = "UNLOCK"
                        setTextColor(Color.BLACK)
                        val bg = GradientDrawable()
                        bg.setColor(Color.WHITE)
                        bg.cornerRadius = 60f
                        background = bg
                        gravity = Gravity.CENTER
                        textSize = 16f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        letterSpacing = 0.1f
                        setPadding(80, 40, 80, 40)
                        
                        setOnClickListener {
                            handleUnlockRequest()
                        }
                    }
                }
                visibility = View.GONE
            }
            
            addView(unlockButton, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, 
                FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 150
            })
            
            errorLockIcon = ImageView(this@BlackScreenService).apply {
                setImageResource(R.drawable.ic_lock)
                setColorFilter(Color.RED)
                visibility = View.GONE
            }
            addView(errorLockIcon, FrameLayout.LayoutParams(120, 120).apply {
                gravity = Gravity.CENTER
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
                            smartAutomationManager.handleManualDismiss()
                            handleUnlockRequest()
                        } else {
                            isUnlockScreenVisible = true
                            tapCount = 0
                            
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
        val config = smartAutomationManager.settings.getConfig()
        if (config.hideFloatingButton) {
            floatingView?.visibility = View.GONE
        } else {
            floatingView?.visibility = View.VISIBLE
        }
        val size = (150 * config.floatingLockSize).toInt()
        val padding = (24 * config.floatingLockSize).toInt()
        
        floatingIconView?.apply {
            layoutParams = FrameLayout.LayoutParams(size, size)
            setPadding(padding, padding, padding, padding)
            
            val iconRes = when (config.floatingLockStyle) {
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

    private fun showBlackoutInternal(showUnlockPageImmediately: Boolean = false) {
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
                }

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
                    
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                            WindowManager.LayoutParams.FLAG_FULLSCREEN,
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
                intent.putExtra("showUnlockPageImmediately", showUnlockPageImmediately)
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
