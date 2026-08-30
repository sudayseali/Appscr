package com.noxscreen.app

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
class NoxTileService : TileService() {

    private var stateJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        updateTileState(BlackScreenService.isRunning)

        stateJob?.cancel()
        stateJob = CoroutineScope(Dispatchers.Main).launch {
            BlackScreenService.isRunningFlow.collect { isRunning ->
                updateTileState(isRunning)
            }
        }
    }

    override fun onStopListening() {
        stateJob?.cancel()
        stateJob = null
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        val isRunning = BlackScreenService.isRunning

        try {
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
        } catch (e: Exception) {
            updateTileState(BlackScreenService.isRunning)
        }
    }

    private fun updateTileState(isRunning: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = if (isRunning) "Stop NoxScreen" else "Start NoxScreen"
        tile.updateTile()
    }

    companion object {
        fun requestTileUpdate(context: android.content.Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    requestListeningState(context, ComponentName(context, NoxTileService::class.java))
                } catch (_: Exception) {}
            }
        }
    }
}
