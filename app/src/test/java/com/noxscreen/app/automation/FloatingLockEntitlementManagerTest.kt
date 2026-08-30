package com.noxscreen.app.automation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

class FakeClock(
    var time: Long = 100000000L,
    var elapsed: Long = 50000000L
) : Clock {
    override fun currentTimeMillis() = time
    override fun elapsedRealtime() = elapsed
    fun advance(ms: Long) {
        time += ms
        elapsed += ms
    }
}

@RunWith(RobolectricTestRunner::class)
class FloatingLockEntitlementManagerTest {

    private lateinit var context: Context
    private lateinit var clock: FakeClock
    private lateinit var manager: FloatingLockEntitlementManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        clock = FakeClock()
        manager = FloatingLockEntitlementManager(context, clock)
        // Clear prefs
        context.getSharedPreferences("FloatingLockEntitlements", Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences("NoxAutomationPrefs", Context.MODE_PRIVATE).edit().clear().apply()
    }

    @Test
    fun testDefaultStyleIsAlwaysUnlocked() {
        assertTrue(manager.isStyleUnlocked("lock"))
        assertEquals(Long.MAX_VALUE, manager.getStyleExpiration("lock"))
        assertNull(manager.getFormattedRemainingTime("lock"))
    }

    @Test
    fun testLockedStyleIsInitiallyLocked() {
        assertFalse(manager.isStyleUnlocked("diamond"))
        assertEquals(0L, manager.getStyleExpiration("diamond"))
        assertNull(manager.getFormattedRemainingTime("diamond"))
    }

    @Test
    fun testRewardUnlocksOnlySelectedStyle() {
        manager.grantUnlock("diamond")
        assertTrue(manager.isStyleUnlocked("diamond"))
        assertFalse(manager.isStyleUnlocked("fire"))
    }

    @Test
    fun testUnlockGrantsExactly7Days() {
        manager.grantUnlock("diamond")
        assertTrue(manager.isStyleUnlocked("diamond"))
        
        // Advance 6 days 23 hours
        clock.advance(6L * 24 * 60 * 60 * 1000 + 23L * 60 * 60 * 1000)
        assertTrue(manager.isStyleUnlocked("diamond"))
        assertNotNull(manager.getFormattedRemainingTime("diamond"))
        
        // Advance remaining time to cross 7 days
        clock.advance(2L * 60 * 60 * 1000)
        assertFalse(manager.isStyleUnlocked("diamond"))
        assertNull(manager.getFormattedRemainingTime("diamond"))
    }

    @Test
    fun testClockMovedBackwardTamperDetection() {
        manager.grantUnlock("diamond")
        assertTrue(manager.isStyleUnlocked("diamond"))

        // Advance 2 days normally
        clock.advance(2L * 24 * 60 * 60 * 1000)
        assertTrue(manager.isStyleUnlocked("diamond"))

        // Tamper: Roll back clock by 10 days (before unlock time)
        clock.time -= (10L * 24 * 60 * 60 * 1000)
        // Should detect tamper and fail closed
        assertFalse(manager.isStyleUnlocked("diamond"))
    }

    @Test
    fun testClockMovedBackwardAfterVerificationTamperDetection() {
        manager.grantUnlock("diamond")
        assertTrue(manager.isStyleUnlocked("diamond"))

        // Advance 3 days
        clock.advance(3L * 24 * 60 * 60 * 1000)
        assertTrue(manager.isStyleUnlocked("diamond")) // Updates last_wall

        // Tamper: Roll back clock by 1 day (still after unlock, but before last_wall)
        clock.time -= (1L * 24 * 60 * 60 * 1000)
        // Should detect rollback and fail closed
        assertFalse(manager.isStyleUnlocked("diamond"))
    }

    @Test
    fun testMonotonicElapsedExpirationInSameBoot() {
        manager.grantUnlock("diamond")
        assertTrue(manager.isStyleUnlocked("diamond"))

        // Simulate wall clock being kept constant or manipulated, but elapsed monotonic time advances 8 days
        clock.elapsed += 8L * 24 * 60 * 60 * 1000
        assertFalse(manager.isStyleUnlocked("diamond"))
    }

    @Test
    fun testRebootElapsedResetMaintainsValidEntitlement() {
        manager.grantUnlock("diamond")
        assertTrue(manager.isStyleUnlocked("diamond"))

        // Simulate device reboot: elapsedRealtime resets to a small number (e.g., 5 seconds after boot)
        // while wall clock advances 1 day normally
        clock.time += 1L * 24 * 60 * 60 * 1000
        clock.elapsed = 5000L // Small uptime after reboot
        
        assertTrue(manager.isStyleUnlocked("diamond"))
    }

    @Test
    fun testCorruptedPreferenceDataFailsClosed() {
        // Corrupted 0 or negative expiration
        context.getSharedPreferences("FloatingLockEntitlements", Context.MODE_PRIVATE)
            .edit()
            .putLong("unlocked_corrupt", 1000L)
            .putLong("expires_corrupt", -1L)
            .apply()

        assertFalse(manager.isStyleUnlocked("corrupt"))
    }

    @Test
    fun testTwoStylesUnlockedIndependently() {
        manager.grantUnlock("diamond")
        clock.advance(3L * 24 * 60 * 60 * 1000) // 3 days later
        manager.grantUnlock("fire")
        
        assertTrue(manager.isStyleUnlocked("diamond"))
        assertTrue(manager.isStyleUnlocked("fire"))
        
        // Advance 5 days (total 8 days since diamond)
        clock.advance(5L * 24 * 60 * 60 * 1000)
        assertFalse(manager.isStyleUnlocked("diamond")) // Expired
        assertTrue(manager.isStyleUnlocked("fire")) // Still valid for 2 more days
    }

    @Test
    fun testValidateActiveStyleFallsBackToDefaultWhenExpired() {
        val settings = AutomationSettings(context)
        settings.updateConfig(settings.getConfig().copy(floatingLockStyle = "crown"))
        manager.grantUnlock("crown")
        
        manager.validateActiveStyle()
        assertEquals("crown", settings.getConfig().floatingLockStyle)
        
        clock.advance(8L * 24 * 60 * 60 * 1000) // Expire
        manager.validateActiveStyle()
        assertEquals("lock", settings.getConfig().floatingLockStyle)
    }
}
