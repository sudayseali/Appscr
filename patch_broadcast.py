import sys

with open('app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt', 'r') as f:
    content = f.read()

target = """        val intent = android.content.Intent("com.noxscreen.app.SETTINGS_UPDATED")
        context.sendBroadcast(intent)"""

replacement = """        val intent = android.content.Intent("com.noxscreen.app.SETTINGS_UPDATED")
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/automation/AutomationSettings.kt', 'w') as f:
    f.write(content)

