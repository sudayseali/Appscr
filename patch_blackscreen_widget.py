with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "r", encoding="utf-8") as f:
    content = f.read()

target = """        fun updateTile(context: android.content.Context) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                android.service.quicksettings.TileService.requestListeningState(
                    context, 
                    android.content.ComponentName(context, NoxTileService::class.java)
                )
            }
        }"""

replacement = """        fun updateTile(context: android.content.Context) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                android.service.quicksettings.TileService.requestListeningState(
                    context, 
                    android.content.ComponentName(context, NoxTileService::class.java)
                )
            }
            // Update widget as well
            NoxWidgetProvider.updateAllWidgets(context)
        }"""

if 'NoxWidgetProvider.updateAllWidgets' not in content:
    content = content.replace(target, replacement)

with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "w", encoding="utf-8") as f:
    f.write(content)
