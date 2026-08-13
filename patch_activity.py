with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'r') as f:
    content = f.read()

target1 = """        val layoutParams = window.attributes
        layoutParams.screenBrightness = 0f
        layoutParams.buttonBrightness = 0f
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            layoutParams.preferredRefreshRate = 30f
        }
        window.attributes = layoutParams
        
        setContent {
            MyApplicationTheme(darkTheme = true) {
                BlackoutScreen(onUnlock = {"""

replacement1 = """        val layoutParams = window.attributes
        layoutParams.screenBrightness = 0f
        layoutParams.buttonBrightness = 0f
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            layoutParams.preferredRefreshRate = 30f
        }
        window.attributes = layoutParams
        
        val showUnlockPageImmediately = intent.getBooleanExtra("showUnlockPageImmediately", false)
        
        setContent {
            MyApplicationTheme(darkTheme = true) {
                BlackoutScreen(initialShowUnlock = showUnlockPageImmediately, onUnlock = {"""

target2 = """@Composable
fun BlackoutScreen(onUnlock: () -> Unit) {
    val context = LocalContext.current
    val automationSettings = remember { com.noxscreen.app.automation.AutomationSettings(context) }
    val autoConfig = remember { automationSettings.getConfig() }
    
    var tapCount by remember { mutableStateOf(0) }
    var isUnlockScreenVisible by remember { mutableStateOf(false) }"""

replacement2 = """@Composable
fun BlackoutScreen(initialShowUnlock: Boolean = false, onUnlock: () -> Unit) {
    val context = LocalContext.current
    val automationSettings = remember { com.noxscreen.app.automation.AutomationSettings(context) }
    val autoConfig = remember { automationSettings.getConfig() }
    
    var tapCount by remember { mutableStateOf(0) }
    var isUnlockScreenVisible by remember { mutableStateOf(initialShowUnlock) }"""

if target1 in content and target2 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'w') as f:
        f.write(content)
    print("Patched BlackoutActivity")
else:
    print("Targets not found")
