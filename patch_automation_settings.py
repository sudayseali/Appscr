import sys

with open('app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt', 'r') as f:
    content = f.read()

content = content.replace("val tapsToWake: Int = 2", "val tapsToWake: Int = 1")
content = content.replace("tapsToWake = prefs.getInt(\"taps_to_wake\", 2)", "tapsToWake = prefs.getInt(\"taps_to_wake\", 1)")

with open('app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt', 'w') as f:
    f.write(content)
