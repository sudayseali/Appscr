package com.noxscreen.app

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes

@RequiresApi(Build.VERSION_CODES.N)
class NoxTileService : TileService() {

    private var isReceiverRegistered = false

    private val settingsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateTileState()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onStartListening() {
        super.onStartListening()
        LocaleHelper.applyLocale(this)
        registerUpdateReceiver()
        updateTileState()
    }

    override fun onStopListening() {
        super.onStopListening()
        unregisterUpdateReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterUpdateReceiver()
    }

    private fun registerUpdateReceiver() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction("com.noxscreen.app.SETTINGS_UPDATED")
                addAction(Intent.ACTION_LOCALE_CHANGED)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(settingsReceiver, filter, RECEIVER_NOT_EXPORTED)
                } else {
                    registerReceiver(settingsReceiver, filter)
                }
                isReceiverRegistered = true
            } catch (_: Exception) {}
        }
    }

    private fun unregisterUpdateReceiver() {
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(settingsReceiver)
            } catch (_: Exception) {}
            isReceiverRegistered = false
        }
    }

    private fun getLocalizedString(@StringRes resId: Int): String {
        return try {
            val localizedContext = LocaleHelper.applyLocale(this)
            localizedContext.getString(resId)
        } catch (_: Exception) {
            getString(resId)
        }
    }

    override fun onClick() {
        super.onClick()
        val isRunning = BlackScreenService.isRunning
        
        // Optimistically update the tile state
        val tile = qsTile
        if (tile != null) {
            tile.state = if (isRunning) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
            tile.label = if (isRunning) {
                getLocalizedString(R.string.quick_settings_tile_start)
            } else {
                getLocalizedString(R.string.quick_settings_tile_stop)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = if (isRunning) {
                    getLocalizedString(R.string.status_disabled)
                } else {
                    getLocalizedString(R.string.status_active)
                }
                tile.contentDescription = tile.label
            }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                tile?.let {
                    it.state = Tile.STATE_INACTIVE
                    it.label = getLocalizedString(R.string.quick_settings_tile_start)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        it.subtitle = getLocalizedString(R.string.status_disabled)
                        it.contentDescription = it.label
                    }
                    it.updateTile()
                }
                try {
                    val mainIntent = Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        val pendingIntent = PendingIntent.getActivity(
                            this, 0, mainIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
                    it.label = getLocalizedString(R.string.quick_settings_tile_start)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        it.subtitle = getLocalizedString(R.string.status_disabled)
                        it.contentDescription = it.label
                    }
                    it.updateTile()
                }
            }
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isRunning = BlackScreenService.isRunning
        
        tile.state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = if (isRunning) {
            getLocalizedString(R.string.quick_settings_tile_stop)
        } else {
            getLocalizedString(R.string.quick_settings_tile_start)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isRunning) {
                getLocalizedString(R.string.status_active)
            } else {
                getLocalizedString(R.string.status_disabled)
            }
            tile.contentDescription = tile.label
        }
        tile.updateTile()
    }
}
