import re
import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add LanguageSelector at top end of Box in ZenithApp
target_box = """        Box(modifier = Modifier.fillMaxSize().background(ZenithBackground)) {
        Column("""
replacement_box = """        Box(modifier = Modifier.fillMaxSize().background(ZenithBackground)) {
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 48.dp, end = 16.dp)) {
            LanguageSelector()
        }
        Column("""

content = content.replace(target_box, replacement_box)

# 2. Add onCreate locale setting
target_oncreate = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        adsManager = com.noxscreen.app.ads.UnityAdsManager(this)"""
replacement_oncreate = """    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("BlackScreenStats", Context.MODE_PRIVATE)
        val savedLang = prefs.getString("app_language", "en") ?: "en"
        val locale = java.util.Locale(savedLang)
        java.util.Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        
        adsManager = com.noxscreen.app.ads.UnityAdsManager(this)"""

content = content.replace(target_oncreate, replacement_oncreate)

# 3. Append LanguageSelector function
language_func = """
@Composable
fun LanguageSelector() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    
    val languages = listOf(
        "en" to "English",
        "so" to "Somali",
        "ar" to "العربية",
        "bn" to "বাংলা",
        "zh" to "中文",
        "es" to "Español",
        "fr" to "Français",
        "de" to "Deutsch",
        "hi" to "हिन्दी",
        "id" to "Bahasa Indonesia",
        "it" to "Italiano",
        "ja" to "日本語",
        "ko" to "한국어",
        "mr" to "मराठी",
        "pa" to "ਪੰਜਾਬੀ",
        "pt" to "Português",
        "ru" to "Русский",
        "te" to "తెలుగు",
        "tr" to "Türkçe",
        "ur" to "اردو",
        "vi" to "Tiếng Việt",
        "sw" to "Kiswahili",
        "fa" to "فارسی",
        "ta" to "தமிழ்",
        "gu" to "ગુજરાતી"
    )

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Language, contentDescription = "Language", tint = ZenithSecondary)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { (code, name) ->
                DropdownMenuItem(
                    text = { Text(name, color = Color.White) },
                    onClick = {
                        expanded = false
                        setAppLocale(context, code)
                    }
                )
            }
        }
    }
}

fun setAppLocale(context: Context, languageCode: String) {
    val prefs = context.getSharedPreferences("BlackScreenStats", Context.MODE_PRIVATE)
    prefs.edit().putString("app_language", languageCode).apply()
    
    val locale = java.util.Locale(languageCode)
    java.util.Locale.setDefault(locale)
    val resources = context.resources
    val config = resources.configuration
    config.setLocale(locale)
    resources.updateConfiguration(config, resources.displayMetrics)
    if (context is android.app.Activity) {
        context.recreate()
    }
}
"""

if "fun LanguageSelector()" not in content:
    content += language_func

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w', encoding='utf-8') as f:
    f.write(content)
