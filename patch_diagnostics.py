import os

path = "app/src/main/java/com/noxscreen/app/automation/SensorHandler.kt"
with open(path, "r") as f:
    content = f.read()

target1 = """            if (proximitySensor != null && !isProximityActive) {
                isProximityActive = sm.registerListener("""
replacement1 = """            if (proximitySensor != null && !isProximityActive) {
                NoXScreenDiagnostics.log("Sensor", "Registering Proximity")
                isProximityActive = sm.registerListener("""

target2 = """            if (lightSensor != null && !isLightActive) {
                isLightActive = sm.registerListener("""
replacement2 = """            if (lightSensor != null && !isLightActive) {
                NoXScreenDiagnostics.log("Sensor", "Registering Light")
                isLightActive = sm.registerListener("""

target3 = """            val registered = sm.registerListener(
                this,
                accelerometer,
                delay
            )"""
replacement3 = """            NoXScreenDiagnostics.log("Sensor", "Registering Accelerometer")
            val registered = sm.registerListener(
                this,
                accelerometer,
                delay
            )"""

target4 = """        pocketState = newState
        cancelStateTimer()"""
replacement4 = """        NoXScreenDiagnostics.log("Sensor", "PocketState transitioned to: $newState")
        pocketState = newState
        cancelStateTimer()"""

if target1 in content and target2 in content and target3 in content and target4 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    content = content.replace(target3, replacement3)
    content = content.replace(target4, replacement4)
    with open(path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
