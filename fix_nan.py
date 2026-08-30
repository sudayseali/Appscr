import os

path = "app/src/main/java/com/noxscreen/app/automation/SensorHandler.kt"
with open(path, "r") as f:
    content = f.read()

target = """    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {"""
replacement = """    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.values.any { it.isNaN() || it.isInfinite() }) return
        when (event.sensor.type) {"""
if target in content:
    content = content.replace(target, replacement)
    with open(path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
