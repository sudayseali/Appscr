import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target = """                ).apply {
                    screenBrightness = 0f
                    buttonBrightness = 0f
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        preferredRefreshRate = 30f // Suggest lowest standard refresh rate
                    }
                }"""

replacement = """                ).apply {
                    screenBrightness = 0f
                    buttonBrightness = 0f
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        preferredRefreshRate = 30f // Suggest lowest standard refresh rate
                    }
                }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
