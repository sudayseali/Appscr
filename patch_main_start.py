import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """        val intent = Intent(this, BlackScreenService::class.java).apply {
            action = "START_BLACKOUT"
        }"""
replacement = """        val intent = Intent(this, BlackScreenService::class.java).apply {
            action = "START_SERVICE"
        }"""
content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
    f.write(content)
