import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target = """    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager"""

replacement = """    private val settingsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.noxscreen.app.SETTINGS_UPDATED") {
                updateFloatingBubbleStyle()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        
        val filter = IntentFilter("com.noxscreen.app.SETTINGS_UPDATED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(settingsReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(settingsReceiver, filter)
        }
        
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager"""
content = content.replace(target, replacement)

target_destroy = """    override fun onDestroy() {
        super.onDestroy()"""
replacement_destroy = """    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(settingsReceiver)
        } catch (e: Exception) {}"""
content = content.replace(target_destroy, replacement_destroy)

target_show = """    private fun showFloatingBubbleInternal() {
        handler.removeCallbacks(timeUpdater)
        
        val config = smartAutomationManager.settings.getConfig()
        val size = (150 * config.floatingLockSize).toInt()
        val padding = (24 * config.floatingLockSize).toInt()
        
        floatingIconView?.apply {
            layoutParams = FrameLayout.LayoutParams(size, size)
            setPadding(padding, padding, padding, padding)
            
            val iconRes = when (config.floatingLockStyle) {
                "lock" -> R.drawable.ic_lock
                "moon" -> R.drawable.ic_moon
                "circle" -> R.drawable.ic_circle
                "double_circle" -> R.drawable.ic_double_circle
                "key" -> R.drawable.ic_key
                "eye_off" -> R.drawable.ic_eye_off
                else -> R.drawable.ic_moon
            }
            setImageResource(iconRes)
        }"""
        
replacement_show = """    private fun updateFloatingBubbleStyle() {
        val config = smartAutomationManager.settings.getConfig()
        val size = (150 * config.floatingLockSize).toInt()
        val padding = (24 * config.floatingLockSize).toInt()
        
        floatingIconView?.apply {
            layoutParams = FrameLayout.LayoutParams(size, size)
            setPadding(padding, padding, padding, padding)
            
            val iconRes = when (config.floatingLockStyle) {
                "lock" -> R.drawable.ic_lock
                "moon" -> R.drawable.ic_moon
                "circle" -> R.drawable.ic_circle
                "double_circle" -> R.drawable.ic_double_circle
                "key" -> R.drawable.ic_key
                "eye_off" -> R.drawable.ic_eye_off
                else -> R.drawable.ic_moon
            }
            setImageResource(iconRes)
        }
    }

    private fun showFloatingBubbleInternal() {
        handler.removeCallbacks(timeUpdater)
        updateFloatingBubbleStyle()"""
content = content.replace(target_show, replacement_show)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
