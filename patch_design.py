import re

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "r") as f:
    content = f.read()

# 1. Update colors to be more premium
content = re.sub(
    r'val ZenithBackground = Color\(0xFF030A16\)\nval ZenithCard = Color\(0xFF091429\)\nval ZenithAccent = Color\(0xFF00FFB2\).*?\nval ZenithSecondary = Color\(0xFF8C9EFF\).*?\nval ZenithTextMuted = Color\(0xFF7A8DAB\)',
    r'val ZenithBackground = Color(0xFF030A16)\nval ZenithCard = Color(0xFF0F1528)\nval ZenithAccent = Color(0xFF00FFC2)\nval ZenithSecondary = Color(0xFF7B61FF)\nval ZenithTextMuted = Color(0xFF8B92A5)\nval ZenithGradientStart = Color(0xFF070912)\nval ZenithGradientEnd = Color(0xFF111424)',
    content, flags=re.DOTALL
)

# 2. Add gradient background to ZenithApp Main screen
target_box = r'        Box\(modifier = Modifier\n            \.fillMaxSize\(\)\n            \.background\(ZenithBackground\)\n            \.padding\(innerPadding\)\)'
replacement_box = r'''        Box(modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ZenithGradientStart, ZenithGradientEnd)))
            .padding(innerPadding))'''
content = re.sub(target_box, replacement_box, content)

# 3. Upgrade main button
target_btn = r'\.background\(ZenithBackground, CircleShape\)\n\s*\.border\(2\.dp, ZenithAccent\.copy\(alpha = 0\.5f\), CircleShape\)'
replacement_btn = r'.background(Brush.radialGradient(listOf(ZenithBackground, ZenithGradientEnd)), CircleShape)\n            .border(2.dp, Brush.linearGradient(listOf(ZenithAccent, ZenithSecondary)), CircleShape)'
content = re.sub(target_btn, replacement_btn, content)

# 4. Upgrade ImpactCard
target_card = r'\.background\(ZenithCard\)'
replacement_card = r'.background(Brush.verticalGradient(listOf(ZenithCard, Color(0x80131422)))).border(1.dp, Color.White.copy(alpha=0.05f), RoundedCornerShape(20.dp))'
content = re.sub(target_card, replacement_card, content)

# 5. Upgrade ExpandableConfigSection
target_section = r'\.background\(ZenithCard\)\n\s*\.border\(1\.dp, Color\.White\.copy\(alpha = 0\.05f\), RoundedCornerShape\(24\.dp\)\)'
replacement_section = r'.background(Brush.verticalGradient(listOf(ZenithCard, Color(0x80131422)))).border(1.dp, Brush.linearGradient(listOf(Color.White.copy(0.08f), Color.Transparent)), RoundedCornerShape(24.dp))'
content = re.sub(target_section, replacement_section, content)

# 6. Upgrade Level Progress Card
target_progress = r'\.background\(Color\(0xFF131422\), RoundedCornerShape\(12\.dp\)\)\n\s*\.border\(1\.dp, Color\(0xFF2A2E44\), RoundedCornerShape\(12\.dp\)\)'
replacement_progress = r'.background(Brush.verticalGradient(listOf(Color(0xFF1A1C30), Color(0xFF131422))), RoundedCornerShape(12.dp))\n            .border(1.dp, Brush.linearGradient(listOf(Color(0xFF3A3E54), Color(0xFF2A2E44))), RoundedCornerShape(12.dp))'
content = re.sub(target_progress, replacement_progress, content)

# 7. Upgrade switch thumb and track
target_switch = r'checkedThumbColor = ZenithBackground,\n\s*checkedTrackColor = ZenithAccent,\n\s*uncheckedThumbColor = ZenithTextMuted,\n\s*uncheckedTrackColor = ZenithBackground,\n\s*uncheckedBorderColor = ZenithTextMuted'
replacement_switch = r'checkedThumbColor = Color.White,\n                checkedTrackColor = ZenithAccent,\n                uncheckedThumbColor = ZenithTextMuted,\n                uncheckedTrackColor = Color(0xFF1E2136),\n                uncheckedBorderColor = Color.Transparent'
content = re.sub(target_switch, replacement_switch, content)

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "w") as f:
    f.write(content)
print("Design upgraded.")
