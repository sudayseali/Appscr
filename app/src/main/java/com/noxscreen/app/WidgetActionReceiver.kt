package com.noxscreen.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class WidgetActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "TOGGLE_NOX_SCREEN") {
            val isRunning = BlackScreenService.isRunning
            if (isRunning) {
                val serviceIntent = Intent(context, BlackScreenService::class.java).apply {
                    action = "STOP_SERVICE"
                }
                context.startService(serviceIntent)
            } else {
                val serviceIntent = Intent(context, BlackScreenService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
