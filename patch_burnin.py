with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "r", encoding="utf-8") as f:
    content = f.read()

target = """    private fun updateAodInfo() {
        val timeSdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val dateSdf = java.text.SimpleDateFormat("EEE, MMM d", java.util.Locale.getDefault())
        val now = java.util.Date()
        aodClockTextView?.text = timeSdf.format(now)
        aodDateTextView?.text = dateSdf.format(now)
        aodBatteryTextView?.text = "🔋 ${getBatteryPercentage()}%"
    }"""

replacement = """    private fun updateAodInfo() {
        val timeSdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val dateSdf = java.text.SimpleDateFormat("EEE, MMM d", java.util.Locale.getDefault())
        val now = java.util.Date()
        aodClockTextView?.text = timeSdf.format(now)
        aodDateTextView?.text = dateSdf.format(now)
        aodBatteryTextView?.text = "🔋 ${getBatteryPercentage()}%"
        
        val config = smartAutomationManager.settings.getConfig()
        if (config.oledBurnInProtection) {
            val random = java.util.Random()
            val xOffset = random.nextInt(31) - 15 // -15 to +15 pixels
            val yOffset = random.nextInt(31) - 15
            aodContainer?.translationX = xOffset.toFloat()
            aodContainer?.translationY = yOffset.toFloat()
        } else {
            aodContainer?.translationX = 0f
            aodContainer?.translationY = 0f
        }
    }"""

if target in content:
    content = content.replace(target, replacement)
else:
    print("Burn in target not found")

with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "w", encoding="utf-8") as f:
    f.write(content)
