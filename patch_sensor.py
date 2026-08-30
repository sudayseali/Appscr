import os

path = "app/src/main/java/com/noxscreen/app/automation/SensorHandler.kt"
with open(path, "r") as f:
    content = f.read()

target1 = """    fun start(enableProximity: Boolean, enableMotion: Boolean, stationarySec: Int = 10, enableShake: Boolean = false) {"""
replacement1 = """    fun start(enableProximity: Boolean, enableMotion: Boolean, stationarySec: Int = 10, enableShake: Boolean = false) {
        if (com.noxscreen.app.BuildConfig.DEBUG) {
            android.util.Log.d("SensorHandler", "start(enableProximity=$enableProximity, enableMotion=$enableMotion)")
        }"""

target2 = """    fun stop() {"""
replacement2 = """    fun stop() {
        if (com.noxscreen.app.BuildConfig.DEBUG) {
            android.util.Log.d("SensorHandler", "stop() called. Cleaning up sensors.")
        }"""

if target1 in content and target2 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    with open(path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
