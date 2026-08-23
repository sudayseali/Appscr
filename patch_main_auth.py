import re

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace adsManager and add authLauncher
pattern_launcher = r"(private lateinit var adsManager: com\.noxscreen\.app\.ads\.UnityAdsManager)"
replacement_launcher = r"\1\n    private val authLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->\n        if (result.resultCode != android.app.Activity.RESULT_OK) {\n            finish()\n        }\n    }"
content = re.sub(pattern_launcher, replacement_launcher, content, count=1)

# Replace the onResume block
pattern_resume = r"if \(\!com\.noxscreen\.app\.security\.AuthenticationManager\.isAuthenticated\(this\)\) \{.*?startActivity\(intent\)\n        \}"
replacement_resume = """if (!com.noxscreen.app.security.AuthenticationManager.isAuthenticated(this)) {
            com.noxscreen.app.security.AuthenticationManager.setAuthenticating()
            val intent = android.content.Intent(this, BiometricAuthActivity::class.java)
            authLauncher.launch(intent)
        }"""
content = re.sub(pattern_resume, replacement_resume, content, flags=re.DOTALL)

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
    f.write(content)

