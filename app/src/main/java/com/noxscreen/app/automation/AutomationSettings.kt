package com.noxscreen.app.automation

import android.content.Context
import android.content.SharedPreferences

data class AutomationConfig(
    val isTimerEnabled: Boolean = false,
    val timerDurationSeconds: Int = 10,
    val isPocketModeEnabled: Boolean = false,
    val isMotionDetectionEnabled: Boolean = false,
    val stationaryDurationSeconds: Int = 10,
    val isSleepTimerEnabled: Boolean = false,
    val sleepTimerDurationMinutes: Int = 30,
    val isDarkTintEnabled: Boolean = false,
    val isAodEnabled: Boolean = true,
    val clockStyle: String = "default",
    val floatingLockStyle: String = "lock",
    val floatingLockSize: Float = 0.5f,
    val showBatteryPercentage: Boolean = true,
    val use24HourTime: Boolean = false,
    val tapsToWake: Int = 1,
    val hideFloatingButton: Boolean = false,
    val reduceBrightness: Boolean = false,
    val oledBurnInProtection: Boolean = true,
    val isBiometricEnabled: Boolean = false
)

class AutomationSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("NoxAutomationPrefs", Context.MODE_PRIVATE)

    fun getConfig(): AutomationConfig {
        return AutomationConfig(
            isTimerEnabled = prefs.getBoolean("is_timer_enabled", false),
            timerDurationSeconds = prefs.getInt("timer_duration_sec", 10),
            isPocketModeEnabled = prefs.getBoolean("is_pocket_mode_enabled", false),
            isMotionDetectionEnabled = prefs.getBoolean("is_motion_detection_enabled", false),
            stationaryDurationSeconds = prefs.getInt("stationary_duration_sec", 10),
            isSleepTimerEnabled = prefs.getBoolean("is_sleep_timer_enabled", false),
            sleepTimerDurationMinutes = prefs.getInt("sleep_timer_duration_min", 30),
            isDarkTintEnabled = prefs.getBoolean("is_dark_tint_enabled", false),
            isAodEnabled = prefs.getBoolean("is_aod_enabled", true),
            clockStyle = prefs.getString("clock_style", "default") ?: "default",
            floatingLockStyle = prefs.getString("floating_lock_style", "lock") ?: "lock",
            floatingLockSize = prefs.getFloat("floating_lock_size", 0.5f),
            showBatteryPercentage = prefs.getBoolean("show_battery_percentage", true),
            use24HourTime = prefs.getBoolean("use_24_hour_time", false),
            tapsToWake = prefs.getInt("taps_to_wake", 1),
            hideFloatingButton = prefs.getBoolean("hide_floating_button", false),
            reduceBrightness = prefs.getBoolean("reduce_brightness", false),
            oledBurnInProtection = prefs.getBoolean("oled_burn_in_protection", true),
            isBiometricEnabled = prefs.getBoolean("is_biometric_enabled", false)
        )
    }

    fun updateConfig(config: AutomationConfig) {
        prefs.edit()
            .putBoolean("is_timer_enabled", config.isTimerEnabled)
            .putInt("timer_duration_sec", config.timerDurationSeconds)
            .putBoolean("is_pocket_mode_enabled", config.isPocketModeEnabled)
            .putBoolean("is_motion_detection_enabled", config.isMotionDetectionEnabled)
            .putInt("stationary_duration_sec", config.stationaryDurationSeconds)
            .putBoolean("is_sleep_timer_enabled", config.isSleepTimerEnabled)
            .putInt("sleep_timer_duration_min", config.sleepTimerDurationMinutes)
            .putBoolean("is_dark_tint_enabled", config.isDarkTintEnabled)
            .putBoolean("is_aod_enabled", config.isAodEnabled)
            .putString("clock_style", config.clockStyle)
            .putString("floating_lock_style", config.floatingLockStyle)
            .putFloat("floating_lock_size", config.floatingLockSize)
            .putBoolean("show_battery_percentage", config.showBatteryPercentage)
            .putBoolean("use_24_hour_time", config.use24HourTime)
            .putInt("taps_to_wake", config.tapsToWake)
            .putBoolean("hide_floating_button", config.hideFloatingButton)
            .putBoolean("reduce_brightness", config.reduceBrightness)
            .putBoolean("oled_burn_in_protection", config.oledBurnInProtection)
            .putBoolean("is_biometric_enabled", config.isBiometricEnabled)
            .apply()
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

    fun setSleepTimerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_sleep_timer_enabled", enabled).apply()
    }

    fun setSleepTimerDuration(minutes: Int) {
        prefs.edit().putInt("sleep_timer_duration_min", minutes).apply()
    }

    fun setDarkTintEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_dark_tint_enabled", enabled).apply()
    }

    fun setAodEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_aod_enabled", enabled).apply()
    }
}
