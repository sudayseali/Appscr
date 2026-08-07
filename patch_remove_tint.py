import re

# AutomationSettings.kt
with open("app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt", "r", encoding="utf-8") as f:
    content = f.read()

content = re.sub(r'^\s*val isDarkTintEnabled: Boolean = false,\n', '', content, flags=re.MULTILINE)
content = re.sub(r'^\s*isDarkTintEnabled = prefs.getBoolean\("is_dark_tint_enabled", false\),\n', '', content, flags=re.MULTILINE)
content = re.sub(r'^\s*\.putBoolean\("is_dark_tint_enabled", config.isDarkTintEnabled\)\n', '', content, flags=re.MULTILINE)

with open("app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt", "w", encoding="utf-8") as f:
    f.write(content)


# MainActivity.kt
with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Removing the whole ZenithSwitchRow block for privacy tint
target_ui = """                ZenithSwitchRow(stringResource(R.string.privacy_tint), "Dim screen instead of total black", autoConfig.isDarkTintEnabled) { 
                    autoConfig = autoConfig.copy(isDarkTintEnabled = it); automationSettings.updateConfig(autoConfig) 
                }"""
content = content.replace(target_ui, "")
# in case of different formatting:
content = re.sub(r'\s*ZenithSwitchRow\(stringResource\(R\.string\.privacy_tint\).*?updateConfig\(autoConfig\)\s*\}', '', content, flags=re.DOTALL)

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "w", encoding="utf-8") as f:
    f.write(content)

# BlackScreenService.kt
with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "r", encoding="utf-8") as f:
    content = f.read()

target1 = """                blackoutView?.setBackgroundColor(
                    if (config.isDarkTintEnabled) Color.parseColor("#E6000000") else Color.BLACK
                )"""
replacement1 = """                blackoutView?.setBackgroundColor(Color.BLACK)"""
content = content.replace(target1, replacement1)

target2 = """                    if (config.isDarkTintEnabled) PixelFormat.TRANSLUCENT else PixelFormat.OPAQUE
                ).apply {
                    screenBrightness = if (config.isDarkTintEnabled) WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE else 0f
                    buttonBrightness = if (config.isDarkTintEnabled) WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE else 0f"""
replacement2 = """                    PixelFormat.OPAQUE
                ).apply {
                    screenBrightness = 0f
                    buttonBrightness = 0f"""
content = content.replace(target2, replacement2)

with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "w", encoding="utf-8") as f:
    f.write(content)
