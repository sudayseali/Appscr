package com.noxscreen.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val prefs = context.getSharedPreferences("NoxAutomationPrefs", Context.MODE_PRIVATE)
            val wasRunning = prefs.getBoolean("service_was_running", false)
            if (wasRunning && !BlackScreenService.isRunning) {
                com.noxscreen.app.automation.NoXScreenDiagnostics.log("BootReceiver", "Restoring service due to reboot or package replacement")
                val serviceIntent = Intent(context, BlackScreenService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } else {
                com.noxscreen.app.automation.NoXScreenDiagnostics.log("BootReceiver", "Skipping service restore. wasRunning=$wasRunning, isRunning=${BlackScreenService.isRunning}")
            }
        }
    }
}
