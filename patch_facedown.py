with open("app/src/main/java/com/noxscreen/app/automation/SensorHandler.kt", "r", encoding="utf-8") as f:
    content = f.read()

target_vars = """    private var enableFaceDown = false
    private var enableShake = false
    private var faceDownStartTime = 0L
    private var isFaceDownTriggered = false"""

replacement_vars = """    private var enableFaceDown = false
    private var enableShake = false
    private var isFaceDownTriggered = false
    private var faceDownCheckRunnable: Runnable? = null"""

target_logic = """                if (enableFaceDown) {
                    val isFaceDownNow = z < -6.0f && abs(x) < 6.0f && abs(y) < 6.0f
                    if (isFaceDownNow) {
                        if (!isFaceDownTriggered) {
                            if (faceDownStartTime == 0L) {
                                faceDownStartTime = System.currentTimeMillis()
                            } else if (System.currentTimeMillis() - faceDownStartTime > 500L) {
                                onFaceDownDetected?.invoke()
                                isFaceDownTriggered = true
                                faceDownStartTime = 0L
                            }
                        }
                    } else {
                        faceDownStartTime = 0L
                        isFaceDownTriggered = false
                    }
                }"""

replacement_logic = """                if (enableFaceDown) {
                    val isFaceDownNow = z < -7.0f && abs(x) < 5.0f && abs(y) < 5.0f
                    if (isFaceDownNow) {
                        if (!isFaceDownTriggered && faceDownCheckRunnable == null) {
                            faceDownCheckRunnable = Runnable {
                                onFaceDownDetected?.invoke()
                                isFaceDownTriggered = true
                                faceDownCheckRunnable = null
                            }
                            mainHandler.postDelayed(faceDownCheckRunnable!!, 500L)
                        }
                    } else {
                        faceDownCheckRunnable?.let { mainHandler.removeCallbacks(it) }
                        faceDownCheckRunnable = null
                        isFaceDownTriggered = false
                    }
                }"""

if target_vars in content:
    content = content.replace(target_vars, replacement_vars)
else:
    print("vars target not found")

if target_logic in content:
    content = content.replace(target_logic, replacement_logic)
else:
    print("logic target not found")

with open("app/src/main/java/com/noxscreen/app/automation/SensorHandler.kt", "w", encoding="utf-8") as f:
    f.write(content)
