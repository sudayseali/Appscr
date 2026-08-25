import os

path = "/tmp/nox_final_repo/app/src/main/java/com/noxscreen/app/automation/UsageLimitMonitor.kt"
with open(path, "r") as f:
    content = f.read()

target1 = """    private var currentSessionApp = ""
    private var currentSessionStartTime = 0L"""
replacement1 = """    private var currentSessionApp = ""
    private var currentSessionStartTime = 0L
    private var currentSessionInitialOsTime = 0L"""
content = content.replace(target1, replacement1)

target2 = """        if (currentSessionApp != foregroundApp) {
            currentSessionApp = foregroundApp
            currentSessionStartTime = System.currentTimeMillis()
        }"""
replacement2 = """        if (currentSessionApp != foregroundApp) {
            currentSessionApp = foregroundApp
            currentSessionStartTime = System.currentTimeMillis()
            
            // Record the OS reported time at the precise moment this session started
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfDay, endTime)
            val usageStat = stats?.find { it.packageName == foregroundApp }
            currentSessionInitialOsTime = usageStat?.totalTimeInForeground ?: 0L
        }"""
content = content.replace(target2, replacement2)

target3 = """            val totalTimeMs = UsageLimitEvaluator.calculateTotalUsageToday(
                osReportedTimeMs = osReportedTime,
                currentSessionStartTime = currentSessionStartTime,
                currentTimeMs = System.currentTimeMillis(),
                startOfDayMs = startOfDay
            )"""
replacement3 = """            val totalTimeMs = UsageLimitEvaluator.calculateTotalUsageToday(
                osReportedTimeMs = osReportedTime,
                currentSessionStartTime = currentSessionStartTime,
                currentTimeMs = System.currentTimeMillis(),
                startOfDayMs = startOfDay,
                currentSessionInitialOsTimeMs = currentSessionInitialOsTime
            )"""
content = content.replace(target3, replacement3)

with open(path, "w") as f:
    f.write(content)
