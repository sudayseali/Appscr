import os

path = "app/src/main/java/com/noxscreen/app/BlackScreenService.kt"
with open(path, "r") as f:
    content = f.read()

target1 = """        try {
            if (floatingView?.parent != null) windowManager.removeView(floatingView)
            if (blackoutView?.parent != null) windowManager.removeView(blackoutView)
        } catch (e: Exception) { }"""
replacement1 = """        try {
            if (floatingView?.parent != null) {
                windowManager.removeView(floatingView)
            }
            if (blackoutView?.parent != null) {
                windowManager.removeView(blackoutView)
            }
        } catch (e: android.view.WindowManager.BadTokenException) {
        } catch (e: IllegalArgumentException) {
        } catch (e: Exception) { }
        floatingView = null
        blackoutView = null"""

target2 = """            if (blackoutView?.parent != null) {
                com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "OVERLAY_REMOVE_STARTED")
                windowManager.removeView(blackoutView)
                com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "OVERLAY_REMOVE_SUCCESS")
            }
        } catch (e: Exception) { 
            com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "OVERLAY_REMOVE_FAILED", e)
        }"""
replacement2 = """            if (blackoutView?.parent != null) {
                com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "OVERLAY_REMOVE_STARTED")
                windowManager.removeView(blackoutView)
                com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "OVERLAY_REMOVE_SUCCESS")
            }
        } catch (e: android.view.WindowManager.BadTokenException) {
            com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "OVERLAY_REMOVE_FAILED_BAD_TOKEN", e)
        } catch (e: IllegalArgumentException) {
            com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "OVERLAY_REMOVE_FAILED_ILLEGAL_ARG", e)
        } catch (e: Exception) { 
            com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "OVERLAY_REMOVE_FAILED", e)
        }"""

if target1 in content and target2 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    with open(path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
