package com.noxscreen.app

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class NoxTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
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
            try {
                val intent = Intent(this, BlackScreenService::class.java).apply {
                    action = "STOP_SERVICE"
                }
                startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
                tile?.let {
                    it.state = Tile.STATE_INACTIVE
                    it.label = "Start NoxScreen"
                    it.updateTile()
                }
                try {
                    val mainIntent = Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        val pendingIntent = android.app.PendingIntent.getActivity(
                            this, 0, mainIntent,
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                        )
                        startActivityAndCollapse(pendingIntent)
                    } else {
                        @Suppress("DEPRECATION")
                        startActivityAndCollapse(mainIntent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return
            }

            try {
                val intent = Intent(this, BlackScreenService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            } catch (e: Exception) {
                tile?.let {
                    it.state = Tile.STATE_INACTIVE
                    it.label = "Start NoxScreen"
                    it.updateTile()
                }
            }
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isRunning = BlackScreenService.isRunning
        
        tile.state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = if (isRunning) "Stop NoxScreen" else "Start NoxScreen"
        // tile.icon = Icon.createWithResource(this, R.drawable.ic_launcher_foreground)
        tile.updateTile()
    }
}
