import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target_app = """@Composable
fun ZenithApp(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartService: () -> Unit,
    totalTimeSaved: Long,
    usageCount: Int
) {"""

replacement_app = """@Composable
fun ZenithApp(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    isServiceRunning: Boolean,
    totalTimeSaved: Long,
    usageCount: Int
) {"""
content = content.replace(target_app, replacement_app)

target_button = """            PowerPulseButton(onClick = onStartService)"""
replacement_button = """            PowerPulseButton(
                onClick = {
                    if (isServiceRunning) onStopService() else onStartService()
                },
                isRunning = isServiceRunning
            )"""
content = content.replace(target_button, replacement_button)

target_pulse = """@Composable
fun PowerPulseButton(onClick: () -> Unit) {"""
replacement_pulse = """@Composable
fun PowerPulseButton(onClick: () -> Unit, isRunning: Boolean = false) {"""
content = content.replace(target_pulse, replacement_pulse)

target_pulse_color = """                if (pulse > 1.1f) ZenithAccent.copy(alpha = 0.5f) else Color.Transparent,"""
replacement_pulse_color = """                if (pulse > 1.1f) (if (isRunning) Color(0xFFFF5252) else ZenithAccent).copy(alpha = 0.5f) else Color.Transparent,"""
content = content.replace(target_pulse_color, replacement_pulse_color)

target_pulse_bg = """                .background(ZenithAccent, CircleShape)"""
replacement_pulse_bg = """                .background(if (isRunning) Color(0xFFFF5252) else ZenithAccent, CircleShape)"""
content = content.replace(target_pulse_bg, replacement_pulse_bg)

target_pulse_icon = """            Icon(Icons.Default.PowerSettingsNew, contentDescription = "Start", tint = ZenithBackground, modifier = Modifier.size(48.dp))"""
replacement_pulse_icon = """            Icon(Icons.Default.PowerSettingsNew, contentDescription = if (isRunning) "Stop" else "Start", tint = ZenithBackground, modifier = Modifier.size(48.dp))"""
content = content.replace(target_pulse_icon, replacement_pulse_icon)

target_call = """                    ZenithApp(
                        hasPermission = hasPermission,
                        onRequestPermission = { requestOverlayPermission() },
                        onStartService = { startBlackScreenService() },
                        totalTimeSaved = totalTimeSaved,
                        usageCount = usageCount
                    )"""

replacement_call = """                    var isServiceRunning by remember { mutableStateOf(isServiceRunning()) }
                    
                    LaunchedEffect(Unit) {
                        while(true) {
                            isServiceRunning = isServiceRunning()
                            kotlinx.coroutines.delay(1000)
                        }
                    }

                    ZenithApp(
                        hasPermission = hasPermission,
                        onRequestPermission = { requestOverlayPermission() },
                        onStartService = { 
                            startBlackScreenService()
                            isServiceRunning = true
                        },
                        onStopService = {
                            stopBlackScreenService()
                            isServiceRunning = false
                        },
                        isServiceRunning = isServiceRunning,
                        totalTimeSaved = totalTimeSaved,
                        usageCount = usageCount
                    )"""
content = content.replace(target_call, replacement_call)

target_func = """    private fun startBlackScreenService() {"""
replacement_func = """    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (BlackScreenService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }
    
    private fun stopBlackScreenService() {
        val intent = Intent(this, BlackScreenService::class.java).apply {
            action = "STOP_SERVICE"
        }
        startService(intent)
    }

    private fun startBlackScreenService() {"""
content = content.replace(target_func, replacement_func)

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
    f.write(content)
