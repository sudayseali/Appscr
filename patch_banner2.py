import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('adUnitId = "bannerAd"', 'adUnitId = "Banner_Android"')

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
    f.write(content)

print("Replaced!")
