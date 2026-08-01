import sys

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r') as f:
    content = f.read()

target = """        } catch (e: Exception) {
            // Fallback: If floating button fails, start BlackoutActivity directly
            try {
                val intent = Intent(this, BlackoutActivity::class.java)"""

replacement = """        } catch (e: Exception) {
            android.util.Log.e("BlackScreenService", "Error adding floating view", e)
            // Fallback: If floating button fails, start BlackoutActivity directly
            try {
                val intent = Intent(this, BlackoutActivity::class.java)"""
content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w') as f:
    f.write(content)
