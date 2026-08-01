import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target1 = """    private var floatingIconView: ImageView? = null"""
replacement1 = """    private var floatingIconView: ImageView? = null
    private var floatingLayoutParams: WindowManager.LayoutParams? = null"""
content = content.replace(target1, replacement1)


target2 = """    @SuppressLint("ClickableViewAccessibility")
    private fun setupFloatingView() {
        floatingView = FrameLayout(this).apply {
            val icon = ImageView(this@BlackScreenService).apply {
                setImageResource(R.drawable.ic_moon)
                setBackgroundResource(android.R.drawable.screen_background_dark_transparent)
                setPadding(24, 24, 24, 24)
            }
            floatingIconView = icon
            addView(icon, FrameLayout.LayoutParams(150, 150))
        }

        val params = WindowManager.LayoutParams("""

replacement2 = """    @SuppressLint("ClickableViewAccessibility")
    private fun setupFloatingView() {
        floatingView = FrameLayout(this).apply {
            val icon = ImageView(this@BlackScreenService).apply {
                setImageResource(R.drawable.ic_moon)
                setBackgroundResource(android.R.drawable.screen_background_dark_transparent)
                setPadding(24, 24, 24, 24)
            }
            floatingIconView = icon
            addView(icon, FrameLayout.LayoutParams(150, 150))
        }

        floatingLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        floatingLayoutParams?.gravity = Gravity.TOP or Gravity.START
        floatingLayoutParams?.x = 0
        floatingLayoutParams?.y = 100

        val params = floatingLayoutParams!!"""
content = content.replace(target2, replacement2)

target3 = """        try {
            if (floatingView?.parent == null) {
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
                params.gravity = Gravity.TOP or Gravity.START
                windowManager.addView(floatingView, params)
            }
        } catch (e: Exception) {"""

replacement3 = """        try {
            if (floatingView?.parent == null) {
                windowManager.addView(floatingView, floatingLayoutParams)
            }
        } catch (e: Exception) {"""
content = content.replace(target3, replacement3)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)

