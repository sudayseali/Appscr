with open("app/src/main/java/com/noxscreen/app/automation/SensorHandler.kt", "r") as f:
    content = f.read()

import re

# We will replace the Sensor.TYPE_PROXIMITY block.
target_prox = """            Sensor.TYPE_PROXIMITY -> {
                val distance = event.values[0]
                val maxRange = event.sensor.maximumRange
                val isNear = distance < maxRange
                onProximityChanged?.invoke(isNear)
            }"""

replacement_prox = """            Sensor.TYPE_PROXIMITY -> {
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

if target_prox in content:
    content = content.replace(target_prox, replacement_prox)

# Need to add proximityStartTime and isProximityTriggered variables
target_vars = """    private var faceDownStartTime = 0L"""
replacement_vars = """    private var faceDownStartTime = 0L
    private var proximityStartTime = 0L
    private var isProximityTriggered = false"""

if target_vars in content:
    content = content.replace(target_vars, replacement_vars)

with open("app/src/main/java/com/noxscreen/app/automation/SensorHandler.kt", "w") as f:
    f.write(content)
