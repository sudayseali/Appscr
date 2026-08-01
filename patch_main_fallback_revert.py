import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """    private fun startBlackScreenService() {
        if (!checkOverlayPermission()) {
            requestOverlayPermission()
            return
        }"""

replacement = """    private fun startBlackScreenService() {
        if (!checkOverlayPermission()) {
            val intent = Intent(this, BlackoutActivity::class.java)
            startActivity(intent)
            return
        }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
    f.write(content)

