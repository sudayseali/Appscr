package com.noxscreen.app.automation

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class UsageAnalyticsManager(
    context: Context,
    private val ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("NoxUsageAnalytics", Context.MODE_PRIVATE)

    fun recordActivation() {
        ioScope.launch {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val key = "hour_count_$hour"
            val currentCount = prefs.getInt(key, 0)
            prefs.edit().putInt(key, currentCount + 1).apply()
        }
    }

    fun getSuggestedAutomation(): String? {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentHourCount = prefs.getInt("hour_count_$currentHour", 0)

        var maxHour = -1
        var maxCount = 0
        for (h in 0..23) {
            val cnt = prefs.getInt("hour_count_$h", 0)
            if (cnt > maxCount) {
                maxCount = cnt
                maxHour = h
            }
        }

        if (maxCount >= 3 && (currentHour == maxHour || currentHourCount >= 2)) {
            val formattedTime = when {
                currentHour == 0 -> "12 AM"
                currentHour < 12 -> "$currentHour AM"
                currentHour == 12 -> "12 PM"
                else -> "${currentHour - 12} PM"
            }
            return "You frequently use NoxScreen around $formattedTime. Enable Pocket Mode for auto-blackout!"
        }
        return null
    }
}
