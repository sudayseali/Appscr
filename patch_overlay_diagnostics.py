import os

path = "app/src/main/java/com/noxscreen/app/BlackScreenService.kt"
with open(path, "r") as f:
    content = f.read()

target1 = """                windowManager.addView(blackoutView, params)
                
                blackoutView?.post {"""
replacement1 = """                com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "OVERLAY_CREATE_STARTED")
                windowManager.addView(blackoutView, params)
                com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "OVERLAY_CREATE_SUCCESS")
                
                blackoutView?.post {"""

target2 = """            if (blackoutView?.parent != null) {
                windowManager.removeView(blackoutView)
            }
        } catch (e: Exception) { }"""
replacement2 = """            if (blackoutView?.parent != null) {
                com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "OVERLAY_REMOVE_STARTED")
                windowManager.removeView(blackoutView)
                com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "OVERLAY_REMOVE_SUCCESS")
            }
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
