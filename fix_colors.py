import re

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "r") as f:
    content = f.read()

target = r'val ZenithBackground = Color\(0xFF030A16\).*?val ZenithGradientEnd = Color\(0xFF111424\)'

replacement = """val ZenithBackgroundStart = Color(0xFF00050A)
val ZenithBackgroundEnd = Color(0xFF0A0F1A)
val ZenithCard = Color(0xFF131722)
val ZenithAccent = Color(0xFF00E676)
val ZenithSecondary = Color(0xFF7B61FF)
val ZenithTextMuted = Color(0xFF8B92A5)"""

content = re.sub(target, replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "w") as f:
    f.write(content)
