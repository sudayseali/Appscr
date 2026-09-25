package com.noxscreen.app.automation

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import java.util.concurrent.TimeUnit

/**
 * FloatingLockEntitlementManager
 *
 * Maamusha xaq-u-yeelashada (entitlements) qaababka Floating Lock:
 * 1. Qaababka bilaashka ah ee default-ka ah (free styles).
 * 2. Qaababka lagu furo xayeysiiska (7-day temporary unlock / trial).
 * 3. Ka-hortagga khiyaanada saacadda adigoo isticmaalaya android.os.SystemClock.elapsedRealtime().
 *    Haddii qofku beddelo saacadda taleefanka (System.currentTimeMillis),
 *    elapsedRealtime iyo boot-time tracking ayaa xaqiijinaya in waqtiga 7-da maalmood
 *    aan si fudud loo khiyaameyn karin.
 */
class FloatingLockEntitlementManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "NoxFloatingLockEntitlements"
        
        // 7 maalmood oo milliseken ah
        const val SEVEN_DAYS_MS = 7L * 24 * 60 * 60 * 1000L

        // Qaababka mar kasta bilaashka ah ee aan xayeysiis u baahnayn
        val PERMANENT_FREE_STYLES: Set<String> = setOf("lock", "moon", "circle", "power")

        // Dhammaan qaababka la heli karo ee app-ka
        val ALL_AVAILABLE_STYLES: List<String> = listOf(
            "lock",
            "moon",
            "circle",
            "double_circle",
            "crown",
            "diamond",
            "star",
            "fire",
            "atom",
            "shield_lock",
            "key",
            "eye_off",
            "shield",
            "fingerprint",
            "power",
            "bolt",
            "favorite"
        )
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Hubi in qaab la bixiyay (style) uu hadda furan yahay iyo in kale.
     * Waxay fiirinaysaa:
     * - Haddii uu ku jiro permanent free styles -> TRUE
     * - Haddii lagu furay xayeysiis -> waxay hubinaysaa waqtiga ka haray 7-da maalmood.
     */
    fun isStyleUnlocked(styleName: String): Boolean {
        if (PERMANENT_FREE_STYLES.contains(styleName)) {
            return true
        }

        val expiryTimeWallClock = prefs.getLong("expiry_wall_$styleName", 0L)
        val unlockElapsed = prefs.getLong("unlocked_elapsed_$styleName", 0L)
        val unlockBootTimeWall = prefs.getLong("unlocked_boot_wall_$styleName", 0L)

        if (expiryTimeWallClock == 0L) {
            return false
        }

        val nowWallClock = System.currentTimeMillis()
        val nowElapsed = SystemClock.elapsedRealtime()

        // 1. Hubi saacadda guud (Wall clock check)
        if (nowWallClock >= expiryTimeWallClock) {
            // Waqtigii 7-da maalmood wuu dhacay
            return false
        }

        // 2. Ka-hortagga khiyaanada saacadda (Anti-time cheat verification)
        // Haddii taleefanku uusan dib u kicin (reboot), elapsedRealtime wuxuu noo sheegayaa
        // waqtiga saxda ah ee qalabku shaqaynayay tan iyo markii la furay.
        val currentBootTimeWall = nowWallClock - nowElapsed
        val isSameBootSession = Math.abs(currentBootTimeWall - unlockBootTimeWall) < 120_000L // 2 min threshold

        if (isSameBootSession) {
            val elapsedDurationSinceUnlock = nowElapsed - unlockElapsed
            if (elapsedDurationSinceUnlock >= SEVEN_DAYS_MS) {
                return false
            }
        } else {
            // Qalabku dib ayuu u kacay (rebooted) ama saacadda nidaamka ayaa dib loo celiyay.
            // Haddii saacadda hadda (nowWallClock) ay ka yar tahay waqtigii la furay (boot_wall),
            // waxaa dhacay khiyaano saacad (time manipulation detected)!
            if (nowWallClock < unlockBootTimeWall) {
                return false
            }
        }

        return true
    }

    /**
     * U fur isticmaalaha qaab (style) muddo 7 maalmood ah ka dib markii uu daawado xayeysiiska.
     * Waxay kaydinaysaa saacadda nidaamka (wall clock) iyo saacadda hardware-ka (elapsedRealtime).
     */
    fun unlockStyleFor7Days(styleName: String) {
        val nowWallClock = System.currentTimeMillis()
        val nowElapsed = SystemClock.elapsedRealtime()
        val expiryWallClock = nowWallClock + SEVEN_DAYS_MS
        val bootTimeWall = nowWallClock - nowElapsed

        prefs.edit()
            .putLong("expiry_wall_$styleName", expiryWallClock)
            .putLong("unlocked_elapsed_$styleName", nowElapsed)
            .putLong("unlocked_boot_wall_$styleName", bootTimeWall)
            .putLong("granted_at_wall_$styleName", nowWallClock)
            .apply()

        // Sidoo kale cusboonaysii AutomationSettings si dhammaan qaybaha app-ku ula socdaan
        syncWithAutomationSettings()
    }

    /**
     * Soo celi waqtiga (millisekanno) ka haray 7-da maalmood ee qaabkaas.
     * Haddii uu bilaash yahay ama uusan furnayn, waxay soo celinaysaa 0 ama Long.MAX_VALUE.
     */
    fun getRemainingTimeMillis(styleName: String): Long {
        if (PERMANENT_FREE_STYLES.contains(styleName)) {
            return Long.MAX_VALUE
        }
        if (!isStyleUnlocked(styleName)) {
            return 0L
        }
        val expiryTimeWallClock = prefs.getLong("expiry_wall_$styleName", 0L)
        val now = System.currentTimeMillis()
        return Math.max(0L, expiryTimeWallClock - now)
    }

    /**
     * Soo celi qoraal kooban oo muujinaya inta maalmood ama saacadood ee ka haray (e.g. "6d 14h left").
     */
    fun getRemainingTimeFormatted(styleName: String): String {
        if (PERMANENT_FREE_STYLES.contains(styleName)) {
            return "Free"
        }
        val remainingMs = getRemainingTimeMillis(styleName)
        if (remainingMs <= 0L) {
            return "Expired"
        }
        val days = TimeUnit.MILLISECONDS.toDays(remainingMs)
        val hours = TimeUnit.MILLISECONDS.toHours(remainingMs) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMs) % 60

        return when {
            days > 0 -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }

    /**
     * Soo saar liiska dhammaan qaababka hadda u furan isticmaalaha (Free + Active 7-day styles).
     */
    fun getAllUnlockedStyles(): Set<String> {
        val unlocked = mutableSetOf<String>()
        unlocked.addAll(PERMANENT_FREE_STYLES)

        for (style in ALL_AVAILABLE_STYLES) {
            if (!PERMANENT_FREE_STYLES.contains(style) && isStyleUnlocked(style)) {
                unlocked.add(style)
            }
        }
        return unlocked
    }

    /**
     * Nadiifi oo xaqiiji in qaababkii dhacay laga saaro AutomationSettings,
     * haddii qaabkii hadda doornaana uu dhacay, dib ugu celi default ("lock").
     */
    fun syncWithAutomationSettings() {
        val autoSettings = AutomationSettings(context)
        val currentConfig = autoSettings.getConfig()
        val validUnlockedStyles = getAllUnlockedStyles()

        var currentSelectedStyle = currentConfig.floatingLockStyle
        if (!validUnlockedStyles.contains(currentSelectedStyle)) {
            currentSelectedStyle = "lock"
        }

        val updatedConfig = currentConfig.copy(
            unlockedStyles = validUnlockedStyles,
            floatingLockStyle = currentSelectedStyle
        )
        autoSettings.updateConfig(updatedConfig)
    }
}
