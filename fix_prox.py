with open("app/src/main/java/com/noxscreen/app/automation/SensorHandler.kt", "r") as f:
    content = f.read()

target = """            Sensor.TYPE_PROXIMITY -> {
                val distance = event.values[0]
                val maxRange = event.sensor.maximumRange
                val isNear = distance < maxRange && distance < 5.0f
                
                if (isNear) {
                    if (proximityStartTime == 0L) {
                        proximityStartTime = System.currentTimeMillis()
                    } else if (System.currentTimeMillis() - proximityStartTime > 500L) {
                        if (!isProximityTriggered) {
                            isProximityTriggered = true
                            onProximityChanged?.invoke(true)
                        }
                    }
                } else {
                    proximityStartTime = 0L
                    if (isProximityTriggered) {
                        isProximityTriggered = false
                        onProximityChanged?.invoke(false)
                    }
                }
            }"""

replacement = """            Sensor.TYPE_PROXIMITY -> {
                val distance = event.values[0]
                val maxRange = event.sensor.maximumRange
                val isNear = distance < maxRange && distance < 5.0f
                
                if (isNear) {
                    if (!isProximityTriggered) {
                        proximityRunnable?.let { mainHandler.removeCallbacks(it) }
                        proximityRunnable = Runnable {
                            isProximityTriggered = true
                            onProximityChanged?.invoke(true)
                        }
                        mainHandler.postDelayed(proximityRunnable!!, 500L)
                    }
                } else {
                    proximityRunnable?.let { mainHandler.removeCallbacks(it) }
                    if (isProximityTriggered) {
                        isProximityTriggered = false
                        onProximityChanged?.invoke(false)
                    }
                }
            }"""

content = content.replace(target, replacement)

# Add proximityRunnable to declarations
content = content.replace("private var proximityStartTime = 0L", "private var proximityRunnable: Runnable? = null")

with open("app/src/main/java/com/noxscreen/app/automation/SensorHandler.kt", "w") as f:
    f.write(content)
