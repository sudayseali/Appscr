import re

with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'r') as f:
    content = f.read()

pattern_launcher = r"class BlackoutActivity : ComponentActivity\(\) \{"
replacement_launcher = """class BlackoutActivity : ComponentActivity() {
    private val authLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            finish()
        } else {
            val errorIntent = android.content.Intent("SHOW_BIOMETRIC_ERROR")
            sendBroadcast(errorIntent)
        }
    }"""
content = re.sub(pattern_launcher, replacement_launcher, content, count=1)

pattern_auth = r"com\.noxscreen\.app\.security\.AuthenticationManager\.startAuthentication\(.*?startActivity\(intent\)"
replacement_auth = """com.noxscreen.app.security.AuthenticationManager.setAuthenticating()
                        val intent = android.content.Intent(this@BlackoutActivity, BiometricAuthActivity::class.java)
                        authLauncher.launch(intent)"""
content = re.sub(pattern_auth, replacement_auth, content, flags=re.DOTALL)

with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'w') as f:
    f.write(content)
