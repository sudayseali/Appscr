import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """            // Central Power Button
            PowerPulseButton(
                onClick = {
                    if (isServiceRunning) onStopService() else onStartService()
                },
                isRunning = isServiceRunning
            )"""

replacement = """            // Central Power Button
            PowerPulseButton(
                onClick = {
                    if (!hasPermission) {
                        onRequestPermission()
                    } else if (isServiceRunning) {
                        onStopService()
                    } else {
                        onStartService()
                    }
                },
                isRunning = isServiceRunning
            )"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Replaced button action!")
else:
    print("Button action not found")
