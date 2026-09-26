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
    private val isOverlayCurrentlyActive: () -> Boolean = { false },
    private val onTriggerBlock: () -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private var isMonitoring = false
    private var isCurrentlyBlocked = false
    private var hasShownWarning = false
    
    private var currentSessionApp = ""
    private var currentSessionStartTime = 0L
    private var lastQueryEndTime = 0L
    private var lastKnownForegroundApp = ""
    private val appUsageTimes = mutableMapOf<String, Long>()

    private val monitorRunnable = object : Runnable {
        override fun run() {
            if (!shouldRunMonitoring()) {
                stopMonitoring()
                return
            }
            checkUsageLimits()
            if (isMonitoring) {
                handler.postDelayed(this, 1000) // Check every 1 second only when Focus Mode is active
            }
        }
    }

    fun shouldRunMonitoring(): Boolean {
        val config = automationSettings.getConfig()
        return (config.isUsageLimitsEnabled || config.isScheduleEnabled) && config.blockedApps.isNotEmpty()
    }

    fun syncWithConfig() {
        if (shouldRunMonitoring()) {
            startMonitoring()
        } else {
            stopMonitoring()
        }
    }

    fun startMonitoring() {
        if (!shouldRunMonitoring()) {
            stopMonitoring()
            return
        }
        if (isMonitoring) return
        isMonitoring = true
        lastQueryEndTime = 0L
        handler.post(monitorRunnable)
    }

    fun stopMonitoring() {
        isMonitoring = false
        handler.removeCallbacks(monitorRunnable)
        isCurrentlyBlocked = false
        hasShownWarning = false
        currentSessionApp = ""
        currentSessionStartTime = 0L
        lastQueryEndTime = 0L
        lastKnownForegroundApp = ""
        appUsageTimes.clear()
    }

    private fun checkUsageLimits() {
        val config = automationSettings.getConfig()
        
        if ((!config.isUsageLimitsEnabled && !config.isScheduleEnabled) || config.blockedApps.isEmpty()) {
            isCurrentlyBlocked = false
            return
        }

        // If blackout overlay is already covering the screen, avoid repeated queries and block triggers
        if (isOverlayCurrentlyActive()) {
            return
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return
        
        val endTime = System.currentTimeMillis()
        // Query a 15-second delta window after initial 1-hour bootstrap to minimize Binder IPC & GC
        val startTime = if (lastQueryEndTime > 0L && (endTime - lastQueryEndTime) < 60_000L) {
            (lastQueryEndTime - 2_000L).coerceAtLeast(endTime - 15_000L)
        } else {
            endTime - 1000L * 60L * 60L
        }
        lastQueryEndTime = endTime

        val usageEvents = try {
            usageStatsManager.queryEvents(startTime, endTime)
        } catch (e: Exception) {
            return
        }
        
        var foregroundApp = lastKnownForegroundApp
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
        lastKnownForegroundApp = foregroundApp

        if (foregroundApp.isEmpty() || !config.blockedApps.contains(foregroundApp)) {
            if (currentSessionApp.isNotEmpty() && currentSessionStartTime > 0) {
                val duration = System.currentTimeMillis() - currentSessionStartTime
                appUsageTimes[currentSessionApp] = (appUsageTimes[currentSessionApp] ?: 0L) + duration
            }
            isCurrentlyBlocked = false
            hasShownWarning = false
            currentSessionApp = ""
            currentSessionStartTime = 0L
            return
        }

        // We are currently in a blocked app.
        if (currentSessionApp != foregroundApp) {
            if (currentSessionApp.isNotEmpty() && currentSessionStartTime > 0) {
                val duration = System.currentTimeMillis() - currentSessionStartTime
                appUsageTimes[currentSessionApp] = (appUsageTimes[currentSessionApp] ?: 0L) + duration
            }
            currentSessionApp = foregroundApp
            currentSessionStartTime = System.currentTimeMillis()
        }

        var shouldBlock = false
        var warningMessage = ""

        // Check Schedule First
        if (config.isScheduleEnabled) {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)
            val currentTotalMinutes = currentHour * 60 + currentMinute
            
            val startTotalMinutes = config.scheduleStartTimeHour * 60 + config.scheduleStartTimeMinute
            val endTotalMinutes = config.scheduleEndTimeHour * 60 + config.scheduleEndTimeMinute
            
            val isWithinSchedule = if (startTotalMinutes <= endTotalMinutes) {
                currentTotalMinutes in startTotalMinutes until endTotalMinutes
            } else {
                // Crosses midnight
                currentTotalMinutes >= startTotalMinutes || currentTotalMinutes < endTotalMinutes
            }
            
            if (isWithinSchedule) {
                shouldBlock = true
            }
        }

        // Check Usage Limit
        if (!shouldBlock && config.isUsageLimitsEnabled) {
            val currentSessionDuration = System.currentTimeMillis() - currentSessionStartTime
            val totalTimeMs = (appUsageTimes[foregroundApp] ?: 0L) + currentSessionDuration
            val limitMs = config.usageLimitDurationMinutes * 60 * 1000L
            val remainingMs = limitMs - totalTimeMs
            
            if (remainingMs <= 0) {
                shouldBlock = true
            } else if (remainingMs in 1..10000) { // 10 seconds soft warning
                warningMessage = "⚠️ Waqtigaagu wuu dhamaanayaa (10s left)!"
            }
        }

        if (shouldBlock) {
            if (!isCurrentlyBlocked) {
                isCurrentlyBlocked = true
                triggerBlockAction()
            } else {
                triggerBlockAction() // keep kicking them out
            }
        } else if (warningMessage.isNotEmpty()) {
            if (!hasShownWarning) {
                hasShownWarning = true
                Toast.makeText(context, warningMessage, Toast.LENGTH_LONG).show()
            }
        } else {
            isCurrentlyBlocked = false
        }
    }

    private fun triggerBlockAction() {
        // A. Display Control (Overlay/Dim/Lock) first so TYPE_APPLICATION_OVERLAY is visible on Android 15+
        onTriggerBlock()

        // B. Stop audio
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val focusRequest = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE).build()
                    audioManager.requestAudioFocus(focusRequest)
                } else {
                    @Suppress("DEPRECATION")
                    audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // C. Remove focus from blocked app (go to home screen safely)
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(homeIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
