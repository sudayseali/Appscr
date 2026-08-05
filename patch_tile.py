with open("app/src/main/java/com/noxscreen/app/NoxTileService.kt", "r", encoding="utf-8") as f:
    content = f.read()

target = """    override fun onClick() {
        super.onClick()
        val isRunning = BlackScreenService.isRunning
        
        if (isRunning) {
            val intent = Intent(this, BlackScreenService::class.java).apply {
                action = "STOP_SERVICE"
            }
            startService(intent)
        } else {
            val intent = Intent(this, BlackScreenService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
        updateTileState()
    }"""

replacement = """    override fun onClick() {
        super.onClick()
        val isRunning = BlackScreenService.isRunning
        
        // Optimistically update the tile state
        val tile = qsTile
        if (tile != null) {
            tile.state = if (isRunning) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
            tile.label = if (isRunning) "Start NoxScreen" else "Stop NoxScreen"
            tile.updateTile()
        }
        
        if (isRunning) {
            val intent = Intent(this, BlackScreenService::class.java).apply {
                action = "STOP_SERVICE"
            }
            startService(intent)
        } else {
            val intent = Intent(this, BlackScreenService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/noxscreen/app/NoxTileService.kt", "w", encoding="utf-8") as f:
    f.write(content)
