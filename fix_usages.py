import re

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace('ZenithBackground, ZenithGradientEnd', 'ZenithBackgroundStart, ZenithBackgroundEnd')
content = content.replace('ZenithCard, ZenithBackground', 'ZenithCard, ZenithBackgroundEnd')
content = content.replace('ZenithBackground else Color.White', 'Color(0xFF00050A) else Color.White')
content = content.replace('.background(ZenithBackground)', '.background(Brush.verticalGradient(listOf(ZenithBackgroundStart, ZenithBackgroundEnd)))')

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "w") as f:
    f.write(content)
