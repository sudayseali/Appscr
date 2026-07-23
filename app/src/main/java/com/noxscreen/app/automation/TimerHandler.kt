package com.noxscreen.app.automation

import android.os.Handler
import android.os.Looper

class TimerHandler {
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    var isTimerRunning: Boolean = false
        private set

    fun startTimer(durationSeconds: Int, onTimerFinished: () -> Unit) {
        cancelTimer()
        if (durationSeconds <= 0) {
            onTimerFinished()
            return
        }
        isTimerRunning = true
        timerRunnable = Runnable {
            isTimerRunning = false
            onTimerFinished()
        }
        handler.postDelayed(timerRunnable!!, durationSeconds * 1000L)
    }

    fun cancelTimer() {
        timerRunnable?.let { handler.removeCallbacks(it) }
        timerRunnable = null
        isTimerRunning = false
    }
}
