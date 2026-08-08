with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "r") as f:
    content = f.read()

import re

# We will use regex to replace it
pattern = re.compile(r'blackoutView\?\.systemUiVisibility\s*=\s*\([\s\S]*?View\.SYSTEM_UI_FLAG_FULLSCREEN\s*\)')

replacement_ui = """if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    blackoutView?.windowInsetsController?.let { controller ->
                        controller.hide(android.view.WindowInsets.Type.systemBars())
                        controller.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                } else {
                    @Suppress("DEPRECATION")
                    blackoutView?.systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
                }"""

content = pattern.sub(replacement_ui, content)

with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "w") as f:
    f.write(content)
