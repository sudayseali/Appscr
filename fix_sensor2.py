with open("app/src/main/java/com/noxscreen/app/automation/SensorHandler.kt", "r") as f:
    content = f.read()

target = """        if ((enableMotion || enableFaceDown || enableShake) && accelerometer != null) {
            isMotionActive = sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            scheduleStationaryCheck()
        }"""

replacement = """        if ((enableMotion || enableFaceDown || enableShake) && accelerometer != null) {
            isMotionActive = sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            if (enableMotion) {
                scheduleStationaryCheck()
            }
        }"""

if target in content:
    content = content.replace(target, replacement)

with open("app/src/main/java/com/noxscreen/app/automation/SensorHandler.kt", "w") as f:
    f.write(content)
