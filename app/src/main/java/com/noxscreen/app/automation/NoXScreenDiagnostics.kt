package com.noxscreen.app.automation

import android.util.Log
import com.noxscreen.app.BuildConfig

object NoXScreenDiagnostics {
    private const val TAG = "NoXScreenDiagnostics"

    fun log(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.e(TAG, "[$tag] $message", throwable)
            } else {
                Log.d(TAG, "[$tag] $message")
            }
        }
    }
}
