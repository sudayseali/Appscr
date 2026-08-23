sed -i 's/private lateinit var adsManager.*/private lateinit var adsManager: com.noxscreen.app.ads.UnityAdsManager\n    private val authLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->\n        if (result.resultCode != android.app.Activity.RESULT_OK) {\n            finish()\n        }\n    }/g' app/src/main/java/com/noxscreen/app/MainActivity.kt

sed -i 's/com.noxscreen.app.security.AuthenticationManager.startAuthentication(/com.noxscreen.app.security.AuthenticationManager.setAuthenticating()\n            val intent = android.content.Intent(this, BiometricAuthActivity::class.java)\n            authLauncher.launch(intent)\n            \/\/ Removed old block/g' app/src/main/java/com/noxscreen/app/MainActivity.kt

# The sed above replaces the startAuthentication call. Let's fix the remainder of the block using python.
