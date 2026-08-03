import sys

with open('app/src/main/java/com/noxscreen/app/automation/UsageLimitMonitor.kt', 'r') as f:
    content = f.read()

target = """    private var hasShownWarning = false
    private var currentSessionApp = ""
    private var currentSessionStartTime = 0L
    private var baseTimeMs = 0L

    private val monitorRunnable = object : Runnable {"""

replacement = """    private var hasShownWarning = false
    private var currentSessionApp = ""
    private var currentSessionStartTime = 0L
    private val appUsageTimes = mutableMapOf<String, Long>()

    private val monitorRunnable = object : Runnable {"""

content = content.replace(target, replacement)


target2 = """        if (foregroundApp.isEmpty() || !config.blockedApps.contains(foregroundApp)) {
            isCurrentlyBlocked = false
            hasShownWarning = false
            currentSessionApp = ""
            return
        }

        // We are currently in a blocked app.
        if (currentSessionApp != foregroundApp) {
            currentSessionApp = foregroundApp
            currentSessionStartTime = System.currentTimeMillis()
            
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, calendar.timeInMillis, endTime)
            baseTimeMs = 0L
            for (stat in stats) {
                if (stat.packageName == foregroundApp) {
                    baseTimeMs = stat.totalTimeInForeground
                }
            }
        }
        val currentSessionDuration = System.currentTimeMillis() - currentSessionStartTime
        val totalTimeMs = baseTimeMs + currentSessionDuration"""

replacement2 = """        if (foregroundApp.isEmpty() || !config.blockedApps.contains(foregroundApp)) {
            if (currentSessionApp.isNotEmpty() && currentSessionStartTime > 0) {
                val duration = System.currentTimeMillis() - currentSessionStartTime
                appUsageTimes[currentSessionApp] = (appUsageTimes[currentSessionApp] ?: 0L) + duration
            }
            isCurrentlyBlocked = false
            hasShownWarning = false
            currentSessionApp = ""
            currentSessionStartTime = 0L
            return
        }

        // We are currently in a blocked app.
        if (currentSessionApp != foregroundApp) {
            if (currentSessionApp.isNotEmpty() && currentSessionStartTime > 0) {
                val duration = System.currentTimeMillis() - currentSessionStartTime
                appUsageTimes[currentSessionApp] = (appUsageTimes[currentSessionApp] ?: 0L) + duration
            }
            currentSessionApp = foregroundApp
            currentSessionStartTime = System.currentTimeMillis()
        }
        
        val currentSessionDuration = System.currentTimeMillis() - currentSessionStartTime
        val totalTimeMs = (appUsageTimes[foregroundApp] ?: 0L) + currentSessionDuration"""

content = content.replace(target2, replacement2)

target3 = """    fun stopMonitoring() {
        isMonitoring = false
        handler.removeCallbacks(monitorRunnable)
        isCurrentlyBlocked = false
        hasShownWarning = false
        currentSessionApp = ""
    }"""

replacement3 = """    fun stopMonitoring() {
        isMonitoring = false
        handler.removeCallbacks(monitorRunnable)
        isCurrentlyBlocked = false
        hasShownWarning = false
        currentSessionApp = ""
        currentSessionStartTime = 0L
        appUsageTimes.clear()
    }"""

content = content.replace(target3, replacement3)

with open('app/src/main/java/com/noxscreen/app/automation/UsageLimitMonitor.kt', 'w') as f:
    f.write(content)

print("Replaced!")
