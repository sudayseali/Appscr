with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "r", encoding="utf-8") as f:
    content = f.read()

target = """                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    if (config.isDarkTintEnabled) PixelFormat.TRANSLUCENT else PixelFormat.OPAQUE
                ).apply {
                    screenBrightness = 0f
                    buttonBrightness = 0f"""

replacement = """                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    if (config.isDarkTintEnabled) PixelFormat.TRANSLUCENT else PixelFormat.OPAQUE
                ).apply {
                    screenBrightness = if (config.isDarkTintEnabled) WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE else 0f
                    buttonBrightness = if (config.isDarkTintEnabled) WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE else 0f"""

if target in content:
    content = content.replace(target, replacement)
else:
    print("target not found")

target2 = """                blackoutView?.setBackgroundColor(
                    if (config.isDarkTintEnabled) Color.parseColor("#ED0C0C12") else Color.BLACK
                )"""

replacement2 = """                blackoutView?.setBackgroundColor(
                    if (config.isDarkTintEnabled) Color.parseColor("#E6000000") else Color.BLACK
                )"""

if target2 in content:
    content = content.replace(target2, replacement2)
else:
    print("target2 not found")

with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "w", encoding="utf-8") as f:
    f.write(content)
