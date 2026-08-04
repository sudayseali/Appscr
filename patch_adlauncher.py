import sys

with open('app/src/main/java/com/noxscreen/app/AdLauncherActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('"START_BLACK_SCREEN"', '"START_BLACKOUT"')

with open('app/src/main/java/com/noxscreen/app/AdLauncherActivity.kt', 'w') as f:
    f.write(content)

print("Replaced!")
