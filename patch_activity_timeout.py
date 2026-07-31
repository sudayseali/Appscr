import sys

with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'r') as f:
    content = f.read()

target = """    LaunchedEffect(tapCount) {
        if (tapCount > 0 && !isUnlockScreenVisible) {
            delay(1500)
            tapCount = 0
        }
    }"""
replacement = """    LaunchedEffect(tapCount) {
        if (tapCount > 0 && !isUnlockScreenVisible) {
            delay(1500)
            tapCount = 0
        }
    }
    
    LaunchedEffect(isUnlockScreenVisible) {
        if (isUnlockScreenVisible) {
            delay(10000)
            isUnlockScreenVisible = false
        }
    }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'w') as f:
    f.write(content)
