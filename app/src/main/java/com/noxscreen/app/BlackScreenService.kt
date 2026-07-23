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

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBlackoutView() {
        blackoutView = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            
            val hintText = TextView(this@BlackScreenService).apply {
                text = "Double tap to unlock"
                setTextColor(Color.DKGRAY)
                gravity = Gravity.CENTER
                textSize = 16f
            }
            addView(hintText, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, 
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            })
        }

        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                smartAutomationManager.handleManualDismiss()
                showFloatingBubbleInternal()
                return true
            }
        })

        blackoutView?.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun showFloatingBubbleInternal() {
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
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            
            Handler(Looper.getMainLooper()).post {
                android.widget.Toast.makeText(this, "Please grant overlay permission", android.widget.Toast.LENGTH_LONG).show()
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
            }
        } catch (e: Exception) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            Handler(Looper.getMainLooper()).post {
                android.widget.Toast.makeText(this, "Please grant overlay permission", android.widget.Toast.LENGTH_LONG).show()
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
