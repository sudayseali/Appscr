with open("app/src/main/java/com/noxscreen/app/NoxTileService.kt", "r", encoding="utf-8") as f:
    content = f.read()

target = """        val tile = qsTile
        if (tile != null) {
            tile.state = if (isRunning) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
            tile.label = if (isRunning) "Start NoxScreen" else "Stop NoxScreen"
            tile.updateTile()
        }"""

replacement = """        val tile = qsTile
        if (tile != null) {
            tile.state = if (isRunning) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
            tile.label = if (isRunning) "Start NoxScreen" else "Stop NoxScreen"
            tile.updateTile()
        }"""

# It's already there and correct! Wait... if isRunning is true, then we are stopping the service, so we want the tile state to be INACTIVE.
