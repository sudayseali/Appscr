import re
with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

pattern = """        if (intent?.action == "AUTH_SUCCESS_UNLOCK") {"""
replacement = """        }
        if (intent?.action == "AUTH_SUCCESS_UNLOCK") {"""

content = content.replace(pattern, replacement)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
