package com.noxscreen.app.security

import android.content.Context
import com.noxscreen.app.automation.AutomationSettings

enum class AuthState {
    LOCKED,
    AUTHENTICATING,
    UNLOCKED
}

object AuthenticationManager {
    private var currentState: AuthState = AuthState.LOCKED
    private var lastUnlockTime: Long = 0
    private const val SESSION_DURATION_MS = 60 * 1000L

    fun isSecurityEnabled(context: Context): Boolean {
        return AutomationSettings(context).getConfig().isBiometricEnabled
    }

    fun isAuthenticated(context: Context): Boolean {
        if (!isSecurityEnabled(context)) return true
        if (currentState == AuthState.UNLOCKED) {
            if (System.currentTimeMillis() - lastUnlockTime < SESSION_DURATION_MS) {
                return true
            } else {
                currentState = AuthState.LOCKED
            }
        }
        return false
    }

    fun setAuthenticating() {
        currentState = AuthState.AUTHENTICATING
    }

    fun markSuccess() {
        currentState = AuthState.UNLOCKED
        lastUnlockTime = System.currentTimeMillis()
    }

    fun markFailure() {
        currentState = AuthState.LOCKED
    }

    fun lock() {
        currentState = AuthState.LOCKED
    }

    fun getState() = currentState
}
