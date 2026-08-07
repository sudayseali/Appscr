package com.noxscreen.app.automation

import android.content.Context
import android.content.SharedPreferences

data class AutomationConfig(
    val isTimerEnabled: Boolean = false,
    val timerDurationSeconds: Int = 10,
    val isPocketModeEnabled: Boolean = false,
    val isMotionDetectionEnabled: Boolean = false,
    val isFlipToSleepEnabled: Boolean = false,
    val isShakeToWakeEnabled: Boolean = false,
    val stationaryDurationSeconds: Int = 10,
    val isAodEnabled: Boolean = false,
    val clockStyle: String = "default",
    val floatingLockStyle: String = "lock",
    val floatingLockSize: Float = 0.5f,
    val showBatteryPercentage: Boolean = true,
    val use24HourTime: Boolean = false,
    val tapsToWake: Int = 1,
    val hideFloatingButton: Boolean = false,
    val reduceBrightness: Boolean = false,
    val oledBurnInProtection: Boolean = false,
    val isSkipUnlockScreenEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isUsageLimitsEnabled: Boolean = false,
    val usageLimitDurationMinutes: Int = 15,
    val blockedApps: Set<String> = emptySet(),
    val unlockedStyles: Set<String> = setOf("lock", "moon", "circle", "power")
)

class AutomationSettings(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("NoxAutomationPrefs", Context.MODE_PRIVATE)

    fun getConfig(): AutomationConfig {
        return AutomationConfig(
            isTimerEnabled = prefs.getBoolean("is_timer_enabled", false),
            timerDurationSeconds = prefs.getInt("timer_duration_sec", 10),
            isPocketModeEnabled = prefs.getBoolean("is_pocket_mode_enabled", false),
            isMotionDetectionEnabled = prefs.getBoolean("is_motion_detection_enabled", false),
            isFlipToSleepEnabled = prefs.getBoolean("is_flip_to_sleep_enabled", false),
            isShakeToWakeEnabled = prefs.getBoolean("is_shake_to_wake_enabled", false),
            stationaryDurationSeconds = prefs.getInt("stationary_duration_sec", 10),
            isAodEnabled = prefs.getBoolean("is_aod_enabled", false),
            clockStyle = prefs.getString("clock_style", "default") ?: "default",
            floatingLockStyle = prefs.getString("floating_lock_style", "lock") ?: "lock",
            floatingLockSize = prefs.getFloat("floating_lock_size", 0.5f),
            showBatteryPercentage = prefs.getBoolean("show_battery_percentage", true),
            use24HourTime = prefs.getBoolean("use_24_hour_time", false),
            tapsToWake = prefs.getInt("taps_to_wake", 1),
            hideFloatingButton = prefs.getBoolean("hide_floating_button", false),
            reduceBrightness = prefs.getBoolean("reduce_brightness", false),
            oledBurnInProtection = prefs.getBoolean("oled_burn_in_protection", false),
            isSkipUnlockScreenEnabled = prefs.getBoolean("is_skip_unlock_screen_enabled", false),
            isBiometricEnabled = prefs.getBoolean("is_biometric_enabled", false),
            isUsageLimitsEnabled = prefs.getBoolean("is_usage_limits_enabled", false),
            usageLimitDurationMinutes = prefs.getInt("usage_limit_duration_min", 15),
            blockedApps = prefs.getStringSet("blocked_apps", emptySet()) ?: emptySet(),
            unlockedStyles = prefs.getStringSet("unlocked_styles", setOf("lock", "moon", "circle", "power")) ?: setOf("lock", "moon", "circle", "power")
        )
    }

    fun updateConfig(config: AutomationConfig) {
        prefs.edit()
            .putBoolean("is_timer_enabled", config.isTimerEnabled)
            .putInt("timer_duration_sec", config.timerDurationSeconds)
            .putBoolean("is_pocket_mode_enabled", config.isPocketModeEnabled)
            .putBoolean("is_motion_detection_enabled", config.isMotionDetectionEnabled)
            .putBoolean("is_flip_to_sleep_enabled", config.isFlipToSleepEnabled)
            .putBoolean("is_shake_to_wake_enabled", config.isShakeToWakeEnabled)
            .putInt("stationary_duration_sec", config.stationaryDurationSeconds)
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
            .putBoolean("is_skip_unlock_screen_enabled", config.isSkipUnlockScreenEnabled)
            .putBoolean("is_biometric_enabled", config.isBiometricEnabled)
            .putBoolean("is_usage_limits_enabled", config.isUsageLimitsEnabled)
            .putInt("usage_limit_duration_min", config.usageLimitDurationMinutes)
            .putStringSet("blocked_apps", config.blockedApps)
            .putStringSet("unlocked_styles", config.unlockedStyles)
            .apply()
            
        val intent = android.content.Intent("com.noxscreen.app.SETTINGS_UPDATED")
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
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



    fun setDarkTintEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_dark_tint_enabled", enabled).apply()
    }

    fun setAodEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_aod_enabled", enabled).apply()
    }
}
