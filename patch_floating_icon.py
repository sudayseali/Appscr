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
            floatingIconView = icon
            addView(icon, FrameLayout.LayoutParams(150, 150))
        }"""

replacement = """    @SuppressLint("ClickableViewAccessibility")
    private fun setupFloatingView() {
        floatingView = FrameLayout(this).apply {
            val icon = ImageView(this@BlackScreenService).apply {
                setImageResource(R.drawable.ic_moon)
                setBackgroundResource(R.drawable.floating_icon_bg)
                setColorFilter(android.graphics.Color.parseColor("#FFC107"))
                setPadding(32, 32, 32, 32)
                elevation = 16f
            }
            floatingIconView = icon
            addView(icon, FrameLayout.LayoutParams(160, 160))
        }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
        f.write(content)
    print("Replaced setupFloatingView!")
else:
    print("setupFloatingView not found")
