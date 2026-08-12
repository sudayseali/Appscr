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
    private const val SESSION_DURATION_MS = 60 * 1000L // 1 min session (can be extended, but let's keep it short for security)
    
    // Auth callbacks
    private var pendingSuccessAction: (() -> Unit)? = null
    private var pendingFailureAction: (() -> Unit)? = null

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

    fun startAuthentication(onSuccess: () -> Unit, onFailure: () -> Unit) {
        currentState = AuthState.AUTHENTICATING
        pendingSuccessAction = onSuccess
        pendingFailureAction = onFailure
    }

    fun markSuccess() {
        if (currentState == AuthState.AUTHENTICATING) {
            currentState = AuthState.UNLOCKED
            lastUnlockTime = System.currentTimeMillis()
            val action = pendingSuccessAction
            clearPendingActions()
            action?.invoke()
        }
    }

    fun markFailure() {
        if (currentState == AuthState.AUTHENTICATING) {
            currentState = AuthState.LOCKED
            val action = pendingFailureAction
            clearPendingActions()
            action?.invoke()
        }
    }
    
    fun lock() {
        currentState = AuthState.LOCKED
        clearPendingActions()
    }
    
    fun getState() = currentState

    private fun clearPendingActions() {
        pendingSuccessAction = null
        pendingFailureAction = null
    }
}
