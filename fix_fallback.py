import os

path = "app/src/main/java/com/noxscreen/app/BlackScreenService.kt"
with open(path, "r") as f:
    content = f.read()

target = """    private fun fallbackToActivity(showUnlockPageImmediately: Boolean) {
        try {
            val intent = Intent(this, BlackoutActivity::class.java)
                intent.putExtra("showUnlockPageImmediately", showUnlockPageImmediately)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } catch (e2: Exception) {
                com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "FALLBACK_ACTIVITY_FAILED", e2)
            }
    }"""
replacement = """    private fun fallbackToActivity(showUnlockPageImmediately: Boolean) {
        try {
            val intent = Intent(this, BlackoutActivity::class.java)
            intent.putExtra("showUnlockPageImmediately", showUnlockPageImmediately)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        } catch (e2: Exception) {
            com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "FALLBACK_ACTIVITY_FAILED", e2)
        }
    }"""
if target in content:
    content = content.replace(target, replacement)
    with open(path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
