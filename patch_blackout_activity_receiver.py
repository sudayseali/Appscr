import sys

with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'r') as f:
    content = f.read()

target = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)"""

replacement = """    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.noxscreen.app.BIOMETRIC_SUCCESS") {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val filter = IntentFilter("com.noxscreen.app.BIOMETRIC_SUCCESS")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }"""
        
content = content.replace(target, replacement)

target2 = """            MyApplicationTheme(darkTheme = true) {
                BlackoutScreen(onUnlock = { finish() })
            }"""
replacement2 = """            MyApplicationTheme(darkTheme = true) {
                BlackoutScreen(onUnlock = { 
                    val settings = com.noxscreen.app.automation.AutomationSettings(this)
                    val isBiometricEnabled = settings.getConfig().isBiometricEnabled
                    if (isBiometricEnabled) {
                        val intent = Intent(this, BiometricAuthActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                    } else {
                        finish() 
                    }
                })
            }"""
content = content.replace(target2, replacement2)

target3 = """    override fun onCreate(savedInstanceState: Bundle?) {"""
replacement3 = """    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {"""
content = content.replace(target3, replacement3)

with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'w') as f:
    f.write(content)
