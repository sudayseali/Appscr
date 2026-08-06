with open("app/src/main/java/com/noxscreen/app/automation/SensorHandler.kt", "r", encoding="utf-8") as f:
    content = f.read()

target_vars = """    private var enableFaceDown = false
    private var enableShake = false
    private var faceDownStartTime = 0L"""

replacement_vars = """    private var enableFaceDown = false
    private var enableShake = false
    private var faceDownStartTime = 0L
    private var isFaceDownTriggered = false"""

target_logic = """                if (enableFaceDown) {
                    val isFaceDownNow = z < -8.0f && abs(x) < 4.0f && abs(y) < 4.0f
                    if (isFaceDownNow) {
                        if (faceDownStartTime == 0L) {
                            faceDownStartTime = System.currentTimeMillis()
                        } else if (System.currentTimeMillis() - faceDownStartTime > 500L) {
                            onFaceDownDetected?.invoke()
                            faceDownStartTime = 0L // Reset
                        }
                    } else {
                        faceDownStartTime = 0L
                    }
                }"""

replacement_logic = """                if (enableFaceDown) {
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
