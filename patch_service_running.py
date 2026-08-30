import os

path = "app/src/main/java/com/noxscreen/app/BlackScreenService.kt"
with open(path, "r") as f:
    content = f.read()

target1 = """    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {"""
replacement1 = """    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences("NoxAutomationPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("service_was_running", true).apply()"""

target2 = """    override fun onDestroy() {
        isRunning = false"""
replacement2 = """    override fun onDestroy() {
        isRunning = false
        val prefs = getSharedPreferences("NoxAutomationPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("service_was_running", false).apply()"""

if target1 in content and target2 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    with open(path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
