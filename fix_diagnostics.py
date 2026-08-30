import os

path = "app/src/main/java/com/noxscreen/app/automation/NoXScreenDiagnostics.kt"
with open(path, "r") as f:
    content = f.read()

# Make sure it's valid Kotlin
print(content)
