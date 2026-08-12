import re

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

# Add onResume
onresume_code = """
    override fun onResume() {
        super.onResume()
        if (!com.noxscreen.app.security.AuthenticationManager.isAuthenticated(this)) {
            com.noxscreen.app.security.AuthenticationManager.startAuthentication(
                onSuccess = { 
                    // Nothing to do, onResume will just proceed 
                },
                onFailure = { 
                    finish() 
                }
            )
            val intent = android.content.Intent(this, BiometricAuthActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {"""

content = content.replace('    override fun onCreate(savedInstanceState: Bundle?) {', onresume_code)

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
    f.write(content)
