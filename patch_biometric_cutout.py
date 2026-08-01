import sys

with open('app/src/main/java/com/noxscreen/app/BiometricAuthActivity.kt', 'r') as f:
    content = f.read()

target = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)"""

replacement = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.addFlags(
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/BiometricAuthActivity.kt', 'w') as f:
    f.write(content)
