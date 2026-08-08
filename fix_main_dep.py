with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "r") as f:
    content = f.read()

target1 = """        val locale = java.util.Locale(savedLang)
        java.util.Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)"""

replacement1 = """        @Suppress("DEPRECATION")
        val locale = java.util.Locale(savedLang)
        java.util.Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)"""

content = content.replace(target1, replacement1)

target2 = """    val locale = java.util.Locale(languageCode)
    java.util.Locale.setDefault(locale)
    val resources = context.resources
    val config = resources.configuration
    config.setLocale(locale)
    resources.updateConfiguration(config, resources.displayMetrics)"""

replacement2 = """    @Suppress("DEPRECATION")
    val locale = java.util.Locale(languageCode)
    java.util.Locale.setDefault(locale)
    val resources = context.resources
    val config = resources.configuration
    config.setLocale(locale)
    @Suppress("DEPRECATION")
    resources.updateConfiguration(config, resources.displayMetrics)"""
content = content.replace(target2, replacement2)

target3 = """    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {"""

replacement3 = """    @Suppress("DEPRECATION")
    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {"""
content = content.replace(target3, replacement3)

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "w") as f:
    f.write(content)
print("Fixed MainActivity.kt")
