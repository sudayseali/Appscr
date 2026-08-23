package com.noxscreen.app.automation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UsageLimitEvaluatorTest {

    @Test
    fun testSameDaySchedule() {
        // Schedule from 09:00 to 17:00
        val startH = 9
        val startM = 0
        val endH = 17
        val endM = 0

        // At 08:59 -> false
        assertFalse(UsageLimitEvaluator.isWithinSchedule(8, 59, startH, startM, endH, endM))
        // At 09:00 -> true
        assertTrue(UsageLimitEvaluator.isWithinSchedule(9, 0, startH, startM, endH, endM))
        // At 12:30 -> true
        assertTrue(UsageLimitEvaluator.isWithinSchedule(12, 30, startH, startM, endH, endM))
        // At 16:59 -> true
        assertTrue(UsageLimitEvaluator.isWithinSchedule(16, 59, startH, startM, endH, endM))
        // At 17:00 -> false
        assertFalse(UsageLimitEvaluator.isWithinSchedule(17, 0, startH, startM, endH, endM))
    }

    @Test
    fun testCrossMidnightSchedule() {
        // Schedule from 22:00 to 06:00
        val startH = 22
        val startM = 0
        val endH = 6
        val endM = 0

        // At 21:59 -> false
        assertFalse(UsageLimitEvaluator.isWithinSchedule(21, 59, startH, startM, endH, endM))
        // At 22:00 -> true
        assertTrue(UsageLimitEvaluator.isWithinSchedule(22, 0, startH, startM, endH, endM))
        // At 23:59 -> true
        assertTrue(UsageLimitEvaluator.isWithinSchedule(23, 59, startH, startM, endH, endM))
        // At 00:00 -> true
        assertTrue(UsageLimitEvaluator.isWithinSchedule(0, 0, startH, startM, endH, endM))
        // At 05:59 -> true
        assertTrue(UsageLimitEvaluator.isWithinSchedule(5, 59, startH, startM, endH, endM))
        // At 06:00 -> false
        assertFalse(UsageLimitEvaluator.isWithinSchedule(6, 0, startH, startM, endH, endM))
        // At 14:00 -> false
        assertFalse(UsageLimitEvaluator.isWithinSchedule(14, 0, startH, startM, endH, endM))
    }

    @Test
    fun testMidnightRolloverCalculation() {
        val startOfDay = 1000000L // Today 00:00:00
        val sessionStartBeforeMidnight = startOfDay - (10 * 60 * 1000L) // 10 minutes before midnight
        val currentTime = startOfDay + (5 * 60 * 1000L) // 5 minutes after midnight
        val osReportedTimeMs = 0L // OS usage for today is 0

        val totalUsageToday = UsageLimitEvaluator.calculateTotalUsageToday(
            osReportedTimeMs = osReportedTimeMs,
            currentSessionStartTime = sessionStartBeforeMidnight,
            currentTimeMs = currentTime,
            startOfDayMs = startOfDay
        )

        // Only the 5 minutes in today's window should be counted for today
        assertEquals(5 * 60 * 1000L, totalUsageToday)
    }

    @Test
    fun testUsageLimitEvaluation() {
        val limitMinutes = 30
        val limitMs = 30 * 60 * 1000L

        // Usage at 15 minutes -> ALLOWED
        val res1 = UsageLimitEvaluator.evaluateUsageLimit(15 * 60 * 1000L, limitMinutes)
        assertTrue(res1 is UsageEvaluationResult.ALLOWED)

        // Usage with 5 seconds remaining -> WARNING
        val res2 = UsageLimitEvaluator.evaluateUsageLimit(limitMs - 5000L, limitMinutes)
        assertTrue(res2 is UsageEvaluationResult.WARNING)
        assertEquals(5, (res2 as UsageEvaluationResult.WARNING).remainingSeconds)

        // Usage exactly at limit -> BLOCKED
        val res3 = UsageLimitEvaluator.evaluateUsageLimit(limitMs, limitMinutes)
        assertTrue(res3 is UsageEvaluationResult.BLOCKED)

        // Usage exceeding limit -> BLOCKED
        val res4 = UsageLimitEvaluator.evaluateUsageLimit(limitMs + 10000L, limitMinutes)
        assertTrue(res4 is UsageEvaluationResult.BLOCKED)
    }
}
