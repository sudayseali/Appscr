with open("app/src/main/java/com/noxscreen/app/BiometricAuthActivity.kt", "r") as f:
    content = f.read()

replacement = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleFailure()
            }
        })"""

content = content.replace("    override fun onCreate(savedInstanceState: Bundle?) {\n        super.onCreate(savedInstanceState)", replacement)

content = content.replace("""    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        handleFailure()
    }""", "")

with open("app/src/main/java/com/noxscreen/app/BiometricAuthActivity.kt", "w") as f:
    f.write(content)
