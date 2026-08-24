package com.noxscreen.app.automation


import kotlinx.coroutines.launch

import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.withContext

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
    private val mainHandler = Handler(Looper.getMainLooper())
    private var monitorJob: kotlinx.coroutines.Job? = null
    private var isMonitoring = false
    var isCurrentlyBlocked = false
        private set
    private var hasShownWarning = false
    
    private var currentSessionApp = ""
    private var currentSessionStartTime = 0L

    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        monitorJob = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            while (isMonitoring) {
                checkUsageLimits()
                kotlinx.coroutines.delay(5000) // Check every 5 seconds on IO thread to balance battery and responsiveness
            }
        }
    }

    fun stopMonitoring() {
        isMonitoring = false
        monitorJob?.cancel()
        monitorJob = null
        isCurrentlyBlocked = false
        hasShownWarning = false
        currentSessionApp = ""
        currentSessionStartTime = 0L
    }

    private fun checkUsageLimits() {
        val config = automationSettings.getConfig()
        
        if (!config.isUsageLimitsEnabled && !config.isScheduleEnabled || config.blockedApps.isEmpty()) {
            isCurrentlyBlocked = false
            return
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return
        
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 30 * 1000 // Look back 30 seconds only for foreground app detection
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
            currentSessionStartTime = 0L
            return
        }

        // We are currently in a blocked app.
        if (currentSessionApp != foregroundApp) {
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
            
            if (UsageLimitEvaluator.isWithinSchedule(
                    currentHour = currentHour,
                    currentMinute = currentMinute,
                    startHour = config.scheduleStartTimeHour,
                    startMinute = config.scheduleStartTimeMinute,
                    endHour = config.scheduleEndTimeHour,
                    endMinute = config.scheduleEndTimeMinute
                )) {
                shouldBlock = true
            }
        }

        // Check Usage Limit using daily aggregate + current session with midnight rollover protection
        if (!shouldBlock && config.isUsageLimitsEnabled) {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis

            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfDay, endTime)
            val usageStat = stats?.find { it.packageName == foregroundApp }
            val osReportedTime = usageStat?.totalTimeInForeground ?: 0L
            
            val totalTimeMs = UsageLimitEvaluator.calculateTotalUsageToday(
                osReportedTimeMs = osReportedTime,
                currentSessionStartTime = currentSessionStartTime,
                currentTimeMs = System.currentTimeMillis(),
                startOfDayMs = startOfDay
            )

            when (val result = UsageLimitEvaluator.evaluateUsageLimit(totalTimeMs, config.usageLimitDurationMinutes)) {
                is UsageEvaluationResult.BLOCKED -> {
                    shouldBlock = true
                }
                is UsageEvaluationResult.WARNING -> {
                    warningMessage = "⚠️ Waqtigaagu wuu dhamaanayaa (${result.remainingSeconds}s left)!"
                }
                is UsageEvaluationResult.ALLOWED -> {
                    // Normal
                }
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
                mainHandler.post {
                    Toast.makeText(context, warningMessage, Toast.LENGTH_LONG).show()
                }
            }
        } else {
            isCurrentlyBlocked = false
        }
    }

    private fun triggerBlockAction() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(homeIntent)

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

        mainHandler.postDelayed({
            onTriggerBlock()
        }, 500)
    }
}
