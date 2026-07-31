package com.noxscreen.app

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
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
            if (isUnlockScreenVisible) {
                updateAodInfo()
                handler.postDelayed(this, 1000)
            }
        }
    }
    
    private var blackoutStartTime = 0L

    private val channelId = "BlackScreenChannel"
    private val notificationId = 1

    private lateinit var smartAutomationManager: com.noxscreen.app.automation.SmartAutomationManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        smartAutomationManager = com.noxscreen.app.automation.SmartAutomationManager(
            context = this,
            onTriggerOverlay = { _ ->
                showBlackoutInternal()
            },
            onRemoveOverlay = {
                showFloatingBubbleInternal()
            },
            onSleepTimerTick = { remainingSec ->
                val minutes = remainingSec / 60
                val seconds = remainingSec % 60
                val timeStr = String.format("%02d:%02d", minutes, seconds)
                sleepTimerTextView?.post {
                    sleepTimerTextView?.text = "Waqti-xire (Sleep Timer): $timeStr"
                    sleepTimerTextView?.visibility = View.VISIBLE
                }
            },
            onSleepTimerExpired = {
                Handler(Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(
                        this,
                        "Waqti-xire: NoxScreen waa la xirey si batteriga loo baajiyo (Sleep Timer expired)",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    
                    if (blackoutStartTime > 0) {
                        addTimeSaved(System.currentTimeMillis() - blackoutStartTime)
                        blackoutStartTime = 0
                    }
                    try {
                        if (floatingView?.parent != null) windowManager.removeView(floatingView)
                        if (blackoutView?.parent != null) windowManager.removeView(blackoutView)
                    } catch (e: Exception) { }
                    stopSelf()
                }
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
            stopSelf()
            return START_NOT_STICKY
        }
        
        if (intent?.action == "START_BLACKOUT") {
            smartAutomationManager.handleUserActivation()
            smartAutomationManager.startSensors()
        } else {
            showFloatingBubbleInternal()
            smartAutomationManager.startSensors()
        }
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

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFloatingView() {
        floatingView = FrameLayout(this).apply {
            val icon = ImageView(this@BlackScreenService).apply {
                setImageResource(R.drawable.ic_moon)
                setBackgroundResource(android.R.drawable.screen_background_dark_transparent)
                setPadding(24, 24, 24, 24)
            }
            addView(icon, FrameLayout.LayoutParams(150, 150))
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

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

    private fun updateAodInfo() {
        val timeSdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val dateSdf = java.text.SimpleDateFormat("EEE, MMM d", java.util.Locale.getDefault())
        val now = java.util.Date()
        aodClockTextView?.text = timeSdf.format(now)
        aodDateTextView?.text = dateSdf.format(now)
        aodBatteryTextView?.text = "🔋 ${getBatteryPercentage()}%"
    }

    private fun getCurrentFormattedTime(): String {
        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    @SuppressLint("ClickableViewAccessibility")
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
    }

    private fun showFloatingBubbleInternal() {
        handler.removeCallbacks(timeUpdater)
        smartAutomationManager.stopSleepTimer()
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
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
                params.gravity = Gravity.TOP or Gravity.START
                windowManager.addView(floatingView, params)
            }
        } catch (e: Exception) {
            // Fallback: If floating button fails, start BlackoutActivity directly
            try {
                val intent = Intent(this, BlackoutActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
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
                blackoutView?.setBackgroundColor(
                    if (config.isDarkTintEnabled) Color.parseColor("#ED0C0C12") else Color.BLACK
                )

                // Always start pure black
                isUnlockScreenVisible = false
                tapCount = 0
                aodContainer?.visibility = View.GONE
                unlockButton?.visibility = View.GONE
                handler.removeCallbacks(timeUpdater)

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
                )
                windowManager.addView(blackoutView, params)
                blackoutStartTime = System.currentTimeMillis()
                incrementUsageCount()
                smartAutomationManager.startSleepTimerIfEnabled()
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
        super.onDestroy()
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
