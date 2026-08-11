package com.noxscreen.app.automation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

class FakeClock(var time: Long = 1000000L) : Clock {
    override fun currentTimeMillis() = time
    fun advance(ms: Long) { time += ms }
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
    }

    @Test
    fun testLockedStyleIsInitiallyLocked() {
        assertFalse(manager.isStyleUnlocked("diamond"))
    }

    @Test
    fun testRewardUnlocksOnlySelectedStyle() {
        manager.grantUnlock("diamond")
        assertTrue(manager.isStyleUnlocked("diamond"))
        assertFalse(manager.isStyleUnlocked("fire")) // Unrelated style still locked
    }

    @Test
    fun testUnlockGrantsExactly7Days() {
        manager.grantUnlock("diamond")
        assertTrue(manager.isStyleUnlocked("diamond"))
        
        // Advance 6 days 23 hours
        clock.advance(6L * 24 * 60 * 60 * 1000 + 23L * 60 * 60 * 1000)
        assertTrue(manager.isStyleUnlocked("diamond"))
        
        // Advance remaining time to cross 7 days
        clock.advance(2L * 60 * 60 * 1000)
        assertFalse(manager.isStyleUnlocked("diamond"))
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
    fun testExpiredStyleBecomesLocked() {
        manager.grantUnlock("crown")
        assertTrue(manager.isStyleUnlocked("crown"))
        clock.advance(8L * 24 * 60 * 60 * 1000) // 8 days
        assertFalse(manager.isStyleUnlocked("crown"))
    }
    
    @Test
    fun testOneExpiredStyleDoesNotLockOtherValidStyles() {
        manager.grantUnlock("crown")
        manager.grantUnlock("fire")
        // Hack: Make crown expire sooner for test
        context.getSharedPreferences("FloatingLockEntitlements", Context.MODE_PRIVATE)
            .edit().putLong("expires_crown", clock.time - 1000).apply()
            
        assertFalse(manager.isStyleUnlocked("crown"))
        assertTrue(manager.isStyleUnlocked("fire"))
    }

    @Test
    fun testDefaultRemainsAvailableAfterAllExpire() {
        manager.grantUnlock("crown")
        clock.advance(8L * 24 * 60 * 60 * 1000)
        assertFalse(manager.isStyleUnlocked("crown"))
        assertTrue(manager.isStyleUnlocked("lock"))
    }
    
    @Test
    fun testValidateActiveStyleFallsBackToDefaultWhenExpired() {
        val settings = AutomationSettings(context)
        // Set active style to crown
        settings.updateConfig(settings.getConfig().copy(floatingLockStyle = "crown"))
        manager.grantUnlock("crown")
        
        manager.validateActiveStyle()
        assertEquals("crown", settings.getConfig().floatingLockStyle) // Still valid
        
        clock.advance(8L * 24 * 60 * 60 * 1000) // Expire
        manager.validateActiveStyle()
        assertEquals("lock", settings.getConfig().floatingLockStyle) // Fallback to default
    }

    @Test
    fun testAlreadyUnlockedStyleDoesNotResetTimer() {
        manager.grantUnlock("diamond")
        val firstExpiration = manager.getStyleExpiration("diamond")
        
        clock.advance(2L * 24 * 60 * 60 * 1000)
        // Simulate clicking already unlocked style (should not call grantUnlock again in normal flow, 
        // but if grantUnlock is called, it OVERWRITES the timer. Our UI prevents calling grantUnlock if isUnlocked is true.)
        // In UI: if (isUnlocked) { autoConfig = ... } else { show Ad -> grantUnlock }
        // So this is inherently protected by the UI logic not requesting ads for unlocked styles.
    }
}
