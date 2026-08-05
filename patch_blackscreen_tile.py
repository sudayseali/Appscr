with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "r", encoding="utf-8") as f:
    content = f.read()

target1 = """    companion object {
        var isRunning = false
            private set
    }"""

replacement1 = """    companion object {
        var isRunning = false
            private set
            
        fun updateTile(context: android.content.Context) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                android.service.quicksettings.TileService.requestListeningState(
                    context, 
                    android.content.ComponentName(context, NoxTileService::class.java)
                )
            }
        }
    }"""

if 'fun updateTile(context: android.content.Context)' not in content:
    content = content.replace(target1, replacement1)

target2 = """    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true"""

replacement2 = """    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        updateTile(this)"""

if 'updateTile(this)' not in content:
    content = content.replace(target2, replacement2)

target3 = """    override fun onDestroy() {
        isRunning = false"""

replacement3 = """    override fun onDestroy() {
        isRunning = false
        updateTile(this)"""

if 'updateTile(this)' not in content.split('override fun onDestroy()')[1]:
    content = content.replace(target3, replacement3)

with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "w", encoding="utf-8") as f:
    f.write(content)
