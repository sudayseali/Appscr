import sys

with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'r') as f:
    content = f.read()

import_str = """import androidx.activity.ComponentActivity"""
new_import = """import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat"""

content = content.replace(import_str, new_import)

setup_str = """        // Set brightness to minimum
        val layoutParams = window.attributes
        layoutParams.screenBrightness = 0f
        window.attributes = layoutParams"""

new_setup = """        // Set brightness to minimum
        val layoutParams = window.attributes
        layoutParams.screenBrightness = 0f
        window.attributes = layoutParams
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }"""

content = content.replace(setup_str, new_setup)

with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'w') as f:
    f.write(content)
