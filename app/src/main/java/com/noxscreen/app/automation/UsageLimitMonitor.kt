package com.noxscreen.app.automation

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import java.util.Calendar

class UsageLimitMonitor(
    private val context: Context,
    private val automationSettings: AutomationSettings,
    private val onTriggerBlock: () -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private var isMonitoring = false
    private var isCurrentlyBlocked = false
    private var hasShownWarning = false

    private var currentSessionApp = ""
    private var currentSessionStartTime = 0L
    private var baseTimeMs = 0L

    private val monitorRunnable = object : Runnable {
        override fun run() {
            checkUsageLimits()
            if (isMonitoring) {
                handler.postDelayed(this, 1000) // Check every 1 second
            }
        }
    }

    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        handler.post(monitorRunnable)
    }

    fun stopMonitoring() {
        isMonitoring = false
        handler.removeCallbacks(monitorRunnable)
        isCurrentlyBlocked = false
        hasShownWarning = false
        currentSessionApp = ""
    }

    private fun checkUsageLimits() {
        val config = automationSettings.getConfig()
        if (!config.isUsageLimitsEnabled || config.blockedApps.isEmpty()) {
            isCurrentlyBlocked = false
            return
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return
        
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 * 60 // Look back 1 hour
        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        
        var foregroundApp = ""
        val event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                foregroundApp = event.packageName
            } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                if (foregroundApp == event.packageName) {
                    foregroundApp = ""
                }
            }
        }

        if (foregroundApp.isEmpty() || !config.blockedApps.contains(foregroundApp)) {
            isCurrentlyBlocked = false
            hasShownWarning = false
            currentSessionApp = ""
            return
        }

        // We are currently in a blocked app.
        if (currentSessionApp != foregroundApp) {
            currentSessionApp = foregroundApp
            currentSessionStartTime = System.currentTimeMillis()
            
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, calendar.timeInMillis, endTime)
            baseTimeMs = 0L
            for (stat in stats) {
                if (stat.packageName == foregroundApp) {
                    baseTimeMs = stat.totalTimeInForeground
                }
            }
        }

        val currentSessionDuration = System.currentTimeMillis() - currentSessionStartTime
        val totalTimeMs = baseTimeMs + currentSessionDuration
        val limitMs = config.usageLimitDurationMinutes * 60 * 1000L
        val remainingMs = limitMs - totalTimeMs

        if (remainingMs <= 0) {
            if (!isCurrentlyBlocked) {
                isCurrentlyBlocked = true
                triggerBlockAction()
            } else {
                triggerBlockAction() // keep kicking them out
            }
        } else if (remainingMs in 1..10000) { // 10 seconds soft warning
            if (!hasShownWarning) {
                hasShownWarning = true
                Toast.makeText(context, "⚠️ Waqtigaagu wuu dhamaanayaa (10s left)!", Toast.LENGTH_LONG).show()
            }
        } else {
            isCurrentlyBlocked = false
        }
    }

    private fun triggerBlockAction() {
        // A. Remove focus (go to home screen)
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(homeIntent)

        // B. Stop audio
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val focusRequest = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE).build()
                audioManager.requestAudioFocus(focusRequest)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // C. Display Control (Overlay/Dim/Lock)
        // We delay the blackout slightly to make it feel deliberate, not jarring
        handler.postDelayed({
            onTriggerBlock()
        }, 500)
    }
}
