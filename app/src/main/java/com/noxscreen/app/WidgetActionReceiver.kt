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
                try {
                    val serviceIntent = Intent(context, BlackScreenService::class.java).apply {
                        action = "STOP_SERVICE"
                    }
                    context.startService(serviceIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(context)) {
                    try {
                        val mainIntent = Intent(context, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        context.startActivity(mainIntent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return
                }
                try {
                    val serviceIntent = Intent(context, BlackScreenService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } catch (e: Exception) {
                    try {
                        val mainIntent = Intent(context, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        context.startActivity(mainIntent)
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                    }
                }
            }
        }
    }
}
