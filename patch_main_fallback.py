import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """    private fun startBlackScreenService() {
        if (!checkOverlayPermission()) {
            // Even if we don't have overlay permission, we can launch BlackoutActivity!
            val intent = Intent(this, BlackoutActivity::class.java)
            startActivity(intent)
            return
        }
        val intent = Intent(this, BlackScreenService::class.java).apply {
            action = "START_SERVICE"
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            moveTaskToBack(true)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to BlackoutActivity
            val fallbackIntent = Intent(this, BlackoutActivity::class.java)
            startActivity(fallbackIntent)
        }
    }"""

replacement = """    private fun startBlackScreenService() {
        if (!checkOverlayPermission()) {
            requestOverlayPermission()
            return
        }
        val intent = Intent(this, BlackScreenService::class.java).apply {
            action = "START_SERVICE"
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            moveTaskToBack(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
    f.write(content)

