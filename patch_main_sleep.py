import re

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "r", encoding="utf-8") as f:
    content = f.read()

content = re.sub(r' *// Sleep Timer \(Battery Saver\).*?colors = SliderDefaults\.colors\([^)]+\)\n *\) *\}', '', content, flags=re.DOTALL)
content = re.sub(r' *ZenithSwitchRow\(stringResource\(R\.string\.sleep_timer\).*?\}\n(?: *if \(autoConfig\.isSleepTimerEnabled\).*?\}\n)?', '', content, flags=re.DOTALL)


with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "w", encoding="utf-8") as f:
    f.write(content)
