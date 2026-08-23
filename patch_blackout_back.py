import re

with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'r') as f:
    content = f.read()

pattern_back = r"override fun onCreate\(savedInstanceState: Bundle\?\).*?super\.onCreate\(savedInstanceState\)"
replacement_back = """
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Intercept back button to prevent Focus Mode bypass
        val homeIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_HOME)
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)"""
content = re.sub(pattern_back, replacement_back, content, flags=re.DOTALL)

with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'w') as f:
    f.write(content)
