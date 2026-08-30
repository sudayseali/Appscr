package com.noxscreen.app.automation

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.TimeUnit

interface Clock {
    fun currentTimeMillis(): Long
    fun elapsedRealtime(): Long
}

class SystemClock : Clock {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
    override fun elapsedRealtime(): Long = android.os.SystemClock.elapsedRealtime()
}

class FloatingLockEntitlementManager(
    private val context: Context,
    private val clock: Clock = SystemClock()
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("FloatingLockEntitlements", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_STYLE = "lock"
        const val ENTITLEMENT_DURATION_MS = 7L * 24L * 60L * 60L * 1000L // 7 days
        private const val ALLOWED_CLOCK_DRIFT_MS = 5L * 60L * 1000L // 5 minutes tolerance
    }

    fun isStyleUnlocked(styleId: String): Boolean {
        if (styleId == DEFAULT_STYLE) return true

        val expiresAt = getStyleExpiration(styleId)
        if (expiresAt <= 0L) return false

        val currentTime = clock.currentTimeMillis()
        val currentElapsed = clock.elapsedRealtime()

        val unlockedAt = prefs.getLong("unlocked_$styleId", 0L)
        val elapsedAtUnlock = prefs.getLong("elapsed_$styleId", 0L)
        val lastKnownWallTime = prefs.getLong("last_wall_$styleId", unlockedAt)

        // 1. Clock moved backward before the unlock timestamp (tamper detection)
        if (unlockedAt > 0L && currentTime < (unlockedAt - ALLOWED_CLOCK_DRIFT_MS)) {
            return false
        }

        // 2. Clock moved backward relative to the last verified observation (tamper detection)
        if (lastKnownWallTime > 0L && currentTime < (lastKnownWallTime - ALLOWED_CLOCK_DRIFT_MS)) {
            return false
        }

        // 3. Monotonic elapsedRealtime check (valid within current boot cycle where currentElapsed >= elapsedAtUnlock)
        if (elapsedAtUnlock > 0L && currentElapsed >= elapsedAtUnlock) {
            val monotonicDelta = currentElapsed - elapsedAtUnlock
            if (monotonicDelta >= ENTITLEMENT_DURATION_MS) {
                return false // Monotonically expired within this boot cycle
            }
        }

        // 4. Wall clock expiration check
        if (currentTime >= expiresAt) {
            return false
        }

        // Record newest valid wall time if moved forward
        if (currentTime > lastKnownWallTime) {
            prefs.edit().putLong("last_wall_$styleId", currentTime).apply()
        }

        return true
    }

    fun getStyleExpiration(styleId: String): Long {
        if (styleId == DEFAULT_STYLE) return Long.MAX_VALUE
        return prefs.getLong("expires_$styleId", 0L)
    }

    fun grantUnlock(styleId: String) {
        if (styleId == DEFAULT_STYLE) return

        val currentTime = clock.currentTimeMillis()
        val currentElapsed = clock.elapsedRealtime()
        val expiresAt = currentTime + ENTITLEMENT_DURATION_MS

        prefs.edit()
            .putLong("unlocked_$styleId", currentTime)
            .putLong("elapsed_$styleId", currentElapsed)
            .putLong("expires_$styleId", expiresAt)
            .putLong("last_wall_$styleId", currentTime)
            .apply()
    }

    fun validateActiveStyle() {
        val settings = AutomationSettings(context)
        val config = settings.getConfig()
        val activeStyle = config.floatingLockStyle

        if (!isStyleUnlocked(activeStyle)) {
            val newConfig = config.copy(floatingLockStyle = DEFAULT_STYLE)
            settings.updateConfig(newConfig)
        }
    }

    fun getFormattedRemainingTime(styleId: String): String? {
        if (styleId == DEFAULT_STYLE) return null
        if (!isStyleUnlocked(styleId)) return null

        val expiresAt = getStyleExpiration(styleId)
        val currentTime = clock.currentTimeMillis()
        if (currentTime >= expiresAt) return null

        val remainingMs = expiresAt - currentTime
        val days = TimeUnit.MILLISECONDS.toDays(remainingMs)
        val hours = TimeUnit.MILLISECONDS.toHours(remainingMs) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMs) % 60

        return when {
            days > 0 -> "${days}d ${hours}h remaining"
            hours > 0 -> "${hours}h ${minutes}m remaining"
            else -> "${minutes}m remaining"
        }
    }

    fun migrateOldStylesIfNeeded() {
        val migrated = prefs.getBoolean("migrated_old_styles", false)
        if (migrated) return

        val oldPrefs = context.getSharedPreferences("NoxAutomationPrefs", Context.MODE_PRIVATE)
        val oldUnlocked = oldPrefs.getStringSet("unlocked_styles", null)

        if (oldUnlocked != null) {
            val currentTime = clock.currentTimeMillis()
            val currentElapsed = clock.elapsedRealtime()
            val expiresAt = currentTime + ENTITLEMENT_DURATION_MS
            val editor = prefs.edit()
            for (style in oldUnlocked) {
                if (style != DEFAULT_STYLE) {
                    editor.putLong("unlocked_$style", currentTime)
                    editor.putLong("elapsed_$style", currentElapsed)
                    editor.putLong("expires_$style", expiresAt)
                    editor.putLong("last_wall_$style", currentTime)
                }
            }
            editor.putBoolean("migrated_old_styles", true)
            editor.apply()
        }
    }
}
