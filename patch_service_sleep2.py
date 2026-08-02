import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target = """            onSleepTimerExpired = {
                Handler(Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(
                        this,
                        "Waqti-xire: NoxScreen waa la xirey si batteriga loo baajiyo (Sleep Timer expired)",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    
                    if (blackoutStartTime > 0) {
                        addTimeSaved(System.currentTimeMillis() - blackoutStartTime)
                        blackoutStartTime = 0
                    }
                    try {
                        if (floatingView?.parent != null) windowManager.removeView(floatingView)
                        if (blackoutView?.parent != null) windowManager.removeView(blackoutView)
                    } catch (e: Exception) { }
                    stopSelf()
                }
            }"""

replacement = """            onSleepTimerExpired = {
                Handler(Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(
                        this,
                        "Waqti-xire: Muraayada waa la iska daminayaa si batteriga loo xifdiyo.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    
                    try {
                        blackoutView?.let {
                            val params = it.layoutParams as WindowManager.LayoutParams
                            params.flags = params.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON.inv()
                            windowManager.updateViewLayout(it, params)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("BlackScreenService", "Error clearing KEEP_SCREEN_ON", e)
                    }
                }
            }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
        f.write(content)
    print("Replaced!")
else:
    print("Not found")
