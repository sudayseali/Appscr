import sys

with open('app/src/main/java/com/noxscreen/app/AdLauncherActivity.kt', 'r') as f:
    content = f.read()

target = """                if (action == "START_BLACKOUT") {
                    val intent = Intent(this, BlackScreenService::class.java).apply {
                        this.action = "START_BLACKOUT"
                    }
                    startService(intent)
                    val broadcastIntent = Intent("com.noxscreen.app.START_BLACKOUT")
                    sendBroadcast(broadcastIntent)
                }"""

replacement = """                if (action == "START_BLACKOUT") {
                    val intent = Intent(this, BlackScreenService::class.java).apply {
                        this.action = "START_BLACKOUT"
                    }
                    startService(intent)
                    val broadcastIntent = Intent("com.noxscreen.app.START_BLACKOUT")
                    sendBroadcast(broadcastIntent)
                } else if (action == "UNLOCK_SCREEN") {
                    val intent = Intent(this, BlackScreenService::class.java).apply {
                        this.action = "UNLOCK_SCREEN"
                    }
                    startService(intent)
                }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/AdLauncherActivity.kt', 'w') as f:
        f.write(content)
    print("Replaced AdLauncher action!")
else:
    print("AdLauncher action not found")
