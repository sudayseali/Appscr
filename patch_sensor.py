with open("app/src/main/java/com/noxscreen/app/automation/SensorHandler.kt", "r", encoding="utf-8") as f:
    content = f.read()

callbacks = """    var onProximityChanged: ((isNear: Boolean) -> Unit)? = null
    var onStationaryDetected: (() -> Unit)? = null
    var onMotionDetected: (() -> Unit)? = null
    var onFaceDownDetected: (() -> Unit)? = null
    var onShakeDetected: (() -> Unit)? = null"""
content = content.replace("    var onProximityChanged: ((isNear: Boolean) -> Unit)? = null\n    var onStationaryDetected: (() -> Unit)? = null\n    var onMotionDetected: (() -> Unit)? = null", callbacks)

start_fun = """    fun start(enableProximity: Boolean, enableMotion: Boolean, stationarySec: Int = 10, enableFaceDown: Boolean = false, enableShake: Boolean = false) {
        stop()
        this.stationaryDurationMs = stationarySec * 1000L
        this.lastMovementTime = System.currentTimeMillis()
        this.enableFaceDown = enableFaceDown
        this.enableShake = enableShake"""
content = content.replace("""    fun start(enableProximity: Boolean, enableMotion: Boolean, stationarySec: Int = 10) {
        stop()
        this.stationaryDurationMs = stationarySec * 1000L
        this.lastMovementTime = System.currentTimeMillis()""", start_fun)

variables = """    private val movementThreshold = 0.8f // m/s² change threshold
    private val shakeThreshold = 12.0f // m/s² change threshold for shake
    private var enableFaceDown = false
    private var enableShake = false
    private var faceDownStartTime = 0L"""
content = content.replace("    private val movementThreshold = 0.8f // m/s² change threshold", variables)

accel_logic = """            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val delta = abs(magnitude - lastAccelMagnitude)
                lastAccelMagnitude = magnitude

                if (delta > movementThreshold) {
                    lastMovementTime = System.currentTimeMillis()
                    onMotionDetected?.invoke()
                }

                if (enableShake && delta > shakeThreshold) {
                    onShakeDetected?.invoke()
                }

                if (enableFaceDown) {
                    if (z < -7.5f && abs(x) < 3.0f && abs(y) < 3.0f) {
                        if (faceDownStartTime == 0L) {
                            faceDownStartTime = System.currentTimeMillis()
                        } else if (System.currentTimeMillis() - faceDownStartTime > 500) {
                            onFaceDownDetected?.invoke()
                            faceDownStartTime = 0L // reset to avoid continuous triggering
                        }
                    } else {
                        faceDownStartTime = 0L
                    }
                }
            }"""
content = content.replace("""            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val delta = abs(magnitude - lastAccelMagnitude)
                lastAccelMagnitude = magnitude
                if (delta > movementThreshold) {
                    lastMovementTime = System.currentTimeMillis()
                    onMotionDetected?.invoke()
                }
            }""", accel_logic)

if 'enableMotion && accelerometer != null' in content:
    content = content.replace("""        if (enableMotion && accelerometer != null) {""", """        if ((enableMotion || enableFaceDown || enableShake) && accelerometer != null) {""")

with open("app/src/main/java/com/noxscreen/app/automation/SensorHandler.kt", "w", encoding="utf-8") as f:
    f.write(content)
