with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'r', encoding='utf-8') as f:
    content = f.read()

companion_code = """
    companion object {
        var isRunning = false
            private set
    }
"""

if 'companion object' not in content:
    content = content.replace('class BlackScreenService : Service() {', 'class BlackScreenService : Service() {' + companion_code)

    # Set to true in onStartCommand
    content = content.replace('override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {', 
                              'override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {\n        isRunning = true')

    # Set to false in onDestroy
    content = content.replace('override fun onDestroy() {', 
                              'override fun onDestroy() {\n        isRunning = false')

with open('app/src/main/java/com/noxscreen/app/BlackScreenService.kt', 'w', encoding='utf-8') as f:
    f.write(content)
