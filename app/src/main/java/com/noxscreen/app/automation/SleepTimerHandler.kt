package com.noxscreen.app.automation

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SleepTimerState {
    var isRunning by mutableStateOf(false)
    var remainingSeconds by mutableStateOf(0L)
    var totalSeconds by mutableStateOf(0L)
    var activeHandler: SleepTimerHandler? = null

    fun reset() {
        isRunning = false
        remainingSeconds = 0L
        totalSeconds = 0L
        activeHandler = null
    }

    fun extend(minutes: Int) {
        activeHandler?.extendTimer(minutes)
    }

    fun cancel() {
        activeHandler?.stopSleepTimer()
    }
}

class SleepTimerHandler {
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    var isTimerRunning: Boolean = false
        private set

    var remainingSeconds: Long = 0L
        private set

    fun startSleepTimer(
        durationMinutes: Int,
        onTick: (remainingSec: Long) -> Unit,
        onFinished: () -> Unit
    ) {
        stopSleepTimer()
        if (durationMinutes <= 0) {
            onFinished()
            return
        }

        remainingSeconds = durationMinutes * 60L
        isTimerRunning = true
        SleepTimerState.totalSeconds = remainingSeconds
        SleepTimerState.remainingSeconds = remainingSeconds
        SleepTimerState.isRunning = true
        SleepTimerState.activeHandler = this

        timerRunnable = object : Runnable {
            override fun run() {
                if (!isTimerRunning) return
                SleepTimerState.remainingSeconds = remainingSeconds
                SleepTimerState.isRunning = true
                onTick(remainingSeconds)
                if (remainingSeconds <= 0) {
                    isTimerRunning = false
                    SleepTimerState.reset()
                    onFinished()
                } else {
                    remainingSeconds -= 1
                    handler.postDelayed(this, 1000L)
                }
            }
        }

        handler.post(timerRunnable!!)
    }

    fun extendTimer(extraMinutes: Int) {
        if (!isTimerRunning) return
        remainingSeconds += extraMinutes * 60L
        SleepTimerState.totalSeconds += extraMinutes * 60L
        SleepTimerState.remainingSeconds = remainingSeconds
    }

    fun stopSleepTimer() {
        timerRunnable?.let { handler.removeCallbacks(it) }
        timerRunnable = null
        isTimerRunning = false
        remainingSeconds = 0L
        SleepTimerState.reset()
    }
}

