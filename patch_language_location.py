import re

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Remove the LanguageSelector from top of ZenithApp
target_box = """    Box(modifier = Modifier.fillMaxSize().background(ZenithBackground)) {
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 48.dp, end = 16.dp)) {
            LanguageSelector()
        }
        Column("""
replacement_box = """    Box(modifier = Modifier.fillMaxSize().background(ZenithBackground)) {
        Column("""

content = content.replace(target_box, replacement_box)

# 2. Add LanguageRow to Display Settings
target_display_settings = """            ExpandableConfigSection(
                title = stringResource(R.string.display_settings),
                icon = Icons.Default.DisplaySettings,
                isExpanded = true
            ) {"""
replacement_display_settings = """            ExpandableConfigSection(
                title = stringResource(R.string.display_settings),
                icon = Icons.Default.DisplaySettings,
                isExpanded = true
            ) {
                LanguageRow()"""

content = content.replace(target_display_settings, replacement_display_settings)

# 3. Rename LanguageSelector to LanguageRow and change its implementation
target_lang_func = """@Composable
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
}"""
replacement_lang_func = """@Composable
fun LanguageRow() {
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

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clickable { expanded = true },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(stringResource(R.string.language), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(stringResource(R.string.select_language), color = ZenithTextMuted, fontSize = 11.sp, lineHeight = 14.sp)
        }
        
        Box {
            Icon(Icons.Default.Language, contentDescription = "Language", tint = ZenithSecondary)
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
}"""

content = content.replace(target_lang_func, replacement_lang_func)

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w', encoding='utf-8') as f:
    f.write(content)
