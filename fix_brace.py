import os

path = "app/src/main/java/com/noxscreen/app/BlackScreenService.kt"
with open(path, "r") as f:
    content = f.read()

target = """            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    override fun onDestroy() {"""
replacement = """            } catch (e2: Exception) {
                com.noxscreen.app.automation.NoXScreenDiagnostics.log("Overlay", "FALLBACK_ACTIVITY_FAILED", e2)
            }
    }

    override fun onDestroy() {"""

if target in content:
    content = content.replace(target, replacement)
    with open(path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
