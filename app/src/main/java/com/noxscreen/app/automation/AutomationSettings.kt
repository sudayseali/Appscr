package com.noxscreen.app.automation

import android.content.Context
import android.content.SharedPreferences

data class AutomationConfig(
    val isTimerEnabled: Boolean = false,
    val timerDurationSeconds: Int = 10,
    val isPocketModeEnabled: Boolean = false,
    val isMotionDetectionEnabled: Boolean = false,
    val stationaryDurationSeconds: Int = 10
)

class AutomationSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("NoxAutomationPrefs", Context.MODE_PRIVATE)

    fun getConfig(): AutomationConfig {
        return AutomationConfig(
            isTimerEnabled = prefs.getBoolean("is_timer_enabled", false),
            timerDurationSeconds = prefs.getInt("timer_duration_sec", 10),
            isPocketModeEnabled = prefs.getBoolean("is_pocket_mode_enabled", false),
            isMotionDetectionEnabled = prefs.getBoolean("is_motion_detection_enabled", false),
            stationaryDurationSeconds = prefs.getInt("stationary_duration_sec", 10)
        )
    }

    fun setTimerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_timer_enabled", enabled).apply()
    }

    fun setTimerDuration(seconds: Int) {
        prefs.edit().putInt("timer_duration_sec", seconds).apply()
    }

    fun setPocketModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_pocket_mode_enabled", enabled).apply()
    }

    fun setMotionDetectionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_motion_detection_enabled", enabled).apply()
    }

    fun setStationaryDuration(seconds: Int) {
        prefs.edit().putInt("stationary_duration_sec", seconds).apply()
    }
}
