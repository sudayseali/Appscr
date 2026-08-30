import os

path = "app/src/main/java/com/noxscreen/app/BlackScreenService.kt"
with open(path, "r") as f:
    content = f.read()

target1 = """                blackoutStartTime = System.currentTimeMillis()
                incrementUsageCount()
            }
        } catch (e: Exception) {
            // Fallback to Activity if overlay is denied by AppOps
            try {
                val intent = Intent(this, BlackoutActivity::class.java)"""
replacement1 = """                blackoutStartTime = System.currentTimeMillis()
                incrementUsageCount()
            }
        } catch (e: android.view.WindowManager.BadTokenException) {
            com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "OVERLAY_CREATE_FAILED_BAD_TOKEN", e)
            fallbackToActivity(showUnlockPageImmediately)
        } catch (e: SecurityException) {
            com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "OVERLAY_CREATE_FAILED_SECURITY", e)
            fallbackToActivity(showUnlockPageImmediately)
        } catch (e: IllegalStateException) {
            com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "OVERLAY_CREATE_FAILED_ILLEGAL_STATE", e)
            fallbackToActivity(showUnlockPageImmediately)
        } catch (e: Exception) {
            com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "OVERLAY_CREATE_FAILED_UNKNOWN", e)
            fallbackToActivity(showUnlockPageImmediately)
        }
    }
    
    private fun fallbackToActivity(showUnlockPageImmediately: Boolean) {
        try {
            val intent = Intent(this, BlackoutActivity::class.java)"""

target2 = """            try {
                val intent = Intent(this, BlackoutActivity::class.java)
                intent.putExtra("showUnlockPageImmediately", showUnlockPageImmediately)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }"""
replacement2 = """            try {
                val intent = Intent(this, BlackoutActivity::class.java)
                intent.putExtra("showUnlockPageImmediately", showUnlockPageImmediately)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } catch (e2: Exception) {
                com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "FALLBACK_ACTIVITY_FAILED", e2)
            }
    }"""

if target1 in content and target2 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    with open(path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
