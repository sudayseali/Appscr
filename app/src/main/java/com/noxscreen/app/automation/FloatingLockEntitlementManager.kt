package com.noxscreen.app.automation

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.TimeUnit

interface Clock {
    fun currentTimeMillis(): Long
}

class SystemClock : Clock {
    override fun currentTimeMillis() = System.currentTimeMillis()
}

class FloatingLockEntitlementManager(private val context: Context, private val clock: Clock = SystemClock()) {
    private val prefs: SharedPreferences = context.getSharedPreferences("FloatingLockEntitlements", Context.MODE_PRIVATE)
    
    companion object {
        const val DEFAULT_STYLE = "lock"
        const val ENTITLEMENT_DURATION_MS = 7L * 24L * 60L * 60L * 1000L // 7 days
    }

    fun isStyleUnlocked(styleId: String): Boolean {
        if (styleId == DEFAULT_STYLE) return true
        
        val expiresAt = getStyleExpiration(styleId)
        val currentTime = clock.currentTimeMillis()
        
        return currentTime < expiresAt
    }

    fun getStyleExpiration(styleId: String): Long {
        if (styleId == DEFAULT_STYLE) return Long.MAX_VALUE
        
        return prefs.getLong("expires_$styleId", 0L)
    }

    fun grantUnlock(styleId: String) {
        if (styleId == DEFAULT_STYLE) return
        
        val currentTime = clock.currentTimeMillis()
        val expiresAt = currentTime + ENTITLEMENT_DURATION_MS
        
        prefs.edit()
            .putLong("unlocked_$styleId", currentTime)
            .putLong("expires_$styleId", expiresAt)
            .apply()
    }
    
    fun validateActiveStyle() {
        val settings = AutomationSettings(context)
        val config = settings.getConfig()
        val activeStyle = config.floatingLockStyle
        
        if (!isStyleUnlocked(activeStyle)) {
            // Active style expired, fallback to default
            val newConfig = config.copy(floatingLockStyle = DEFAULT_STYLE)
            settings.updateConfig(newConfig)
        }
    }

    fun getFormattedRemainingTime(styleId: String): String? {
        if (styleId == DEFAULT_STYLE) return null
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
            val expiresAt = currentTime + ENTITLEMENT_DURATION_MS
            val editor = prefs.edit()
            for (style in oldUnlocked) {
                if (style != DEFAULT_STYLE) {
                    editor.putLong("unlocked_$style", currentTime)
                    editor.putLong("expires_$style", expiresAt)
                }
            }
            editor.putBoolean("migrated_old_styles", true)
            editor.apply()
        }
    }
}
