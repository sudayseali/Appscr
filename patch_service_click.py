import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target = """                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        smartAutomationManager.handleUserActivation()
                    }
                    true
                }"""

replacement = """                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        // Launch AdActivity for 3 ads before activating
                        val intent = android.content.Intent(this@BlackScreenService, AdLauncherActivity::class.java).apply {
                            putExtra("ADS_COUNT", 3)
                            putExtra("ON_COMPLETE_ACTION", "START_BLACK_SCREEN")
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                    }
                    true
                }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
        f.write(content)
    print("Replaced Service click!")
else:
    print("Target not found in Service")
