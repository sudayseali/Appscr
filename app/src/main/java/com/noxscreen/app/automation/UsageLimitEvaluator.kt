package com.noxscreen.app.automation

sealed class UsageEvaluationResult {
    object ALLOWED : UsageEvaluationResult()
    data class WARNING(val remainingSeconds: Int) : UsageEvaluationResult()
    object BLOCKED : UsageEvaluationResult()
}

object UsageLimitEvaluator {

    fun isWithinSchedule(
        currentHour: Int,
        currentMinute: Int,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ): Boolean {
        val currentTotalMinutes = currentHour * 60 + currentMinute
        val startTotalMinutes = startHour * 60 + startMinute
        val endTotalMinutes = endHour * 60 + endMinute

        return if (startTotalMinutes <= endTotalMinutes) {
            currentTotalMinutes in startTotalMinutes until endTotalMinutes
        } else {
            // Crosses midnight (e.g. 22:00 -> 06:00)
            currentTotalMinutes >= startTotalMinutes || currentTotalMinutes < endTotalMinutes
        }
    }

    fun calculateTotalUsageToday(
        osReportedTimeMs: Long,
        currentSessionStartTime: Long,
        currentTimeMs: Long,
        startOfDayMs: Long
    ): Long {
        if (currentSessionStartTime <= 0L) return osReportedTimeMs

        val effectiveSessionStart = maxOf(currentSessionStartTime, startOfDayMs)
        val currentSessionDuration = if (currentTimeMs >= effectiveSessionStart) {
            currentTimeMs - effectiveSessionStart
        } else {
            0L
        }
        return osReportedTimeMs + currentSessionDuration
    }

    fun evaluateUsageLimit(
        totalTimeUsedMs: Long,
        limitMinutes: Int
    ): UsageEvaluationResult {
        val limitMs = limitMinutes * 60 * 1000L
        val remainingMs = limitMs - totalTimeUsedMs

        return when {
            remainingMs <= 0 -> UsageEvaluationResult.BLOCKED
            remainingMs in 1..10000 -> UsageEvaluationResult.WARNING(remainingSeconds = maxOf(1, (remainingMs / 1000).toInt()))
            else -> UsageEvaluationResult.ALLOWED
        }
    }
}
