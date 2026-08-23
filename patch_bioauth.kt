package com.noxscreen.app

import android.app.Activity
import android.app.PendingIntent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.noxscreen.app.security.AuthenticationManager
import com.noxscreen.app.security.AuthState
import java.util.concurrent.Executor

class BiometricAuthActivity : FragmentActivity() {
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    
    private var isAuthCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (AuthenticationManager.getState() != AuthState.AUTHENTICATING) {
            AuthenticationManager.lock()
            finish()
            return
        }
        
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        
        setContent {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
        }

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    handleFailure()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    handleSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock NoxScreen")
            .setSubtitle("Use your biometric or device lock to unlock")
            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
            
        if (savedInstanceState == null) {
            biometricPrompt.authenticate(promptInfo)
        }
    }
    
    private fun handleSuccess() {
        if (isAuthCompleted) return
        isAuthCompleted = true
        AuthenticationManager.markSuccess()
        
        @Suppress("DEPRECATION")
        intent.getParcelableExtra<PendingIntent>("EXTRA_SUCCESS_INTENT")?.send()
        
        setResult(Activity.RESULT_OK)
        finish()
    }
    
    private fun handleFailure() {
        if (isAuthCompleted) return
        isAuthCompleted = true
        AuthenticationManager.markFailure()
        
        @Suppress("DEPRECATION")
        intent.getParcelableExtra<PendingIntent>("EXTRA_FAILURE_INTENT")?.send()
        
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isAuthCompleted && !isChangingConfigurations) {
            handleFailure()
        }
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        if (!isAuthCompleted) {
            handleFailure()
        }
    }
}
