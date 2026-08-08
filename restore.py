import re

with open("main_backup.kt", "r") as f:
    backup_content = f.read()
    
with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "r") as f:
    current_content = f.read()

# We want to extract ZenithApp from backup and just style it.
# Wait, I already changed colors and gradients globally.
# So I can just take ZenithApp from main_backup.kt, and modify the UI elements in it to match the requested design, but keep the functionality intact.

