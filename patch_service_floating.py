import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target = """    @SuppressLint("ClickableViewAccessibility")
    private fun setupFloatingView() {
        floatingView = FrameLayout(this).apply {
            val icon = ImageView(this@BlackScreenService).apply {
                setImageResource(R.drawable.ic_moon)
                setBackgroundResource(android.R.drawable.screen_background_dark_transparent)
                setPadding(24, 24, 24, 24)
            }
            addView(icon, FrameLayout.LayoutParams(150, 150))
        }"""

replacement = """    private var floatingIconView: ImageView? = null

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFloatingView() {
        floatingView = FrameLayout(this).apply {
            val icon = ImageView(this@BlackScreenService).apply {
                setImageResource(R.drawable.ic_moon)
                setBackgroundResource(android.R.drawable.screen_background_dark_transparent)
                setPadding(24, 24, 24, 24)
            }
            floatingIconView = icon
            addView(icon, FrameLayout.LayoutParams(150, 150))
        }"""

content = content.replace(target, replacement)

target2 = """    private fun showFloatingBubbleInternal() {
        handler.removeCallbacks(timeUpdater)"""

replacement2 = """    private fun showFloatingBubbleInternal() {
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

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
