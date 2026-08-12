import re

with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'r') as f:
    content = f.read()

target = """                    if (tapCount >= autoConfig.tapsToWake) {
                        isUnlockScreenVisible = true
                        tapCount = 0
                    }"""

replacement = """                    if (tapCount >= autoConfig.tapsToWake) {
                        if (autoConfig.isSkipUnlockScreenEnabled) {
                            onUnlock()
                        } else {
                            isUnlockScreenVisible = true
                        }
                        tapCount = 0
                    }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'w') as f:
        f.write(content)
    print("Patched BlackoutActivity")
else:
    print("Target not found")
