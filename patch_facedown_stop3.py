with open("app/src/main/java/com/noxscreen/app/automation/SensorHandler.kt", "r", encoding="utf-8") as f:
    content = f.read()

target = """    fun stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this)
        }
        isProximityActive = false
        isMotionActive = false
        stationaryCheckRunnable?.let { mainHandler.removeCallbacks(it) }
        stationaryCheckRunnable = null
        faceDownCheckRunnable?.let { mainHandler.removeCallbacks(it) }
        faceDownCheckRunnable = null
    }"""

replacement = """    fun stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this)
        }
        isProximityActive = false
        isMotionActive = false
        stationaryCheckRunnable?.let { mainHandler.removeCallbacks(it) }
        stationaryCheckRunnable = null
    }"""

if target in content:
    content = content.replace(target, replacement)
else:
    print("target not found")

with open("app/src/main/java/com/noxscreen/app/automation/SensorHandler.kt", "w", encoding="utf-8") as f:
    f.write(content)
