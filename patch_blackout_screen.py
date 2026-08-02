import sys

with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'r') as f:
    content = f.read()

target = """    LaunchedEffect(isUnlockScreenVisible) {
        if (isUnlockScreenVisible) {
            delay(10000)
            isUnlockScreenVisible = false
        }
    }"""

replacement = """    LaunchedEffect(isUnlockScreenVisible) {
        val window = (context as? android.app.Activity)?.window
        if (isUnlockScreenVisible) {
            window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            delay(10000)
            isUnlockScreenVisible = false
        } else {
            // After 5 seconds of black screen, remove KEEP_SCREEN_ON to allow real device sleep
            delay(5000)
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'w') as f:
    f.write(content)
