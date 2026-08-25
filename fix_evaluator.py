import os

path = "/tmp/nox_final_repo/app/src/main/java/com/noxscreen/app/automation/UsageLimitEvaluator.kt"
with open(path, "r") as f:
    content = f.read()

target = """    fun calculateTotalUsageToday(
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
    }"""

replacement = """    fun calculateTotalUsageToday(
        osReportedTimeMs: Long,
        currentSessionStartTime: Long,
        currentTimeMs: Long,
        startOfDayMs: Long,
        currentSessionInitialOsTimeMs: Long = 0L
    ): Long {
        if (currentSessionStartTime <= 0L) return osReportedTimeMs

        val effectiveSessionStart = maxOf(currentSessionStartTime, startOfDayMs)
        val currentSessionDuration = if (currentTimeMs >= effectiveSessionStart) {
            currentTimeMs - effectiveSessionStart
        } else {
            0L
        }

        return maxOf(osReportedTimeMs, currentSessionInitialOsTimeMs + currentSessionDuration)
    }"""

content = content.replace(target, replacement)

with open(path, "w") as f:
    f.write(content)
