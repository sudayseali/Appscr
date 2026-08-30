import os

path = "app/src/main/java/com/noxscreen/app/BlackScreenService.kt"
with open(path, "r") as f:
    content = f.read()

target = """    override fun onDestroy() {
        isRunning = false
        val prefs = getSharedPreferences("NoxAutomationPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("service_was_running", false).apply()
        updateTile(this)
        super.onDestroy()"""
if target not in content:
    # Try another way
    print("Trying another way")
