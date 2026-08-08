import re

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "r") as f:
    content = f.read()

target = r'\.background\(ZenithBackground\)'
replacement = r'.background(Brush.verticalGradient(listOf(ZenithBackgroundStart, ZenithBackgroundEnd)))'

content = re.sub(target, replacement, content)

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "w") as f:
    f.write(content)
