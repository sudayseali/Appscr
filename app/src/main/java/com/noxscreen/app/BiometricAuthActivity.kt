package com.noxscreen.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.noxscreen.app.security.AppSecurityManager
import java.util.concurrent.Executor

class BiometricAuthActivity : FragmentActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var securityManager: AppSecurityManager
    
    private var isSuccess = false
    private var authTarget: String = "BLACKOUT" // or "APP_LOCK"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        securityManager = AppSecurityManager(this)
        authTarget = intent.getStringExtra("AUTH_TARGET") ?: "BLACKOUT"

        // Hubi haddii taleefanka laga damiyay ama lagala baxay Fingerprint iyo PIN
        val biometricStatus = securityManager.checkBiometricAvailability()
        if (biometricStatus != AppSecurityManager.BiometricStatus.AVAILABLE) {
            val settings = com.noxscreen.app.automation.AutomationSettings(this)
            val cfg = settings.getConfig()
            settings.updateConfig(cfg.copy(isBiometricEnabled = false, isAntiSpyEnabled = false))
            
            Toast.makeText(
                this, 
                "Taleefanka lagama helin Fingerprint ama PIN. Amniga waa la damiyay.", 
                Toast.LENGTH_LONG
            ).show()

            isSuccess = true
            if (authTarget == "BLACKOUT") {
                val serviceIntent = Intent(this, BlackScreenService::class.java).apply {
                    action = "BIOMETRIC_SUCCESS"
                }
                startService(serviceIntent)
                sendBroadcast(Intent("com.noxscreen.app.BIOMETRIC_SUCCESS"))
            } else {
                sendBroadcast(Intent("com.noxscreen.app.APP_LOCK_UNLOCKED"))
            }
            setResult(Activity.RESULT_OK)
            finish()
            return
        }

        // Hubi haddii uu jiro lockout firfircoon (isku dayyo khaldan oo badan)
        if (securityManager.isLockedOut()) {
            val remainingSec = securityManager.getRemainingLockoutSeconds()
            Toast.makeText(
                this, 
                "Amniga: Isku dayyo khaldan oo badan! Sug ${remainingSec}s ka hor intaadan isku dayin.",
                Toast.LENGTH_LONG
            ).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        window.addFlags(
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN or
            android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        
        // Pure solid Black - user sees 0% of the mobile until successfully authenticated
        setContent {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        }

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS ||
                        errorCode == BiometricPrompt.ERROR_HW_NOT_PRESENT ||
                        errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE) {
                        
                        val settings = com.noxscreen.app.automation.AutomationSettings(this@BiometricAuthActivity)
                        val cfg = settings.getConfig()
                        settings.updateConfig(cfg.copy(isBiometricEnabled = false, isAntiSpyEnabled = false))

                        isSuccess = true
                        if (authTarget == "BLACKOUT") {
                            val intent = Intent(this@BiometricAuthActivity, BlackScreenService::class.java).apply {
                                action = "BIOMETRIC_SUCCESS"
                            }
                            startService(intent)
                            sendBroadcast(Intent("com.noxscreen.app.BIOMETRIC_SUCCESS"))
                        } else {
                            sendBroadcast(Intent("com.noxscreen.app.APP_LOCK_UNLOCKED"))
                        }
                        setResult(Activity.RESULT_OK)
                        finish()
                        return
                    }
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    
                    isSuccess = true
                    securityManager.recordSuccess()
                    
                    if (authTarget == "BLACKOUT") {
                        val intent = Intent(this@BiometricAuthActivity, BlackScreenService::class.java).apply {
                            action = "BIOMETRIC_SUCCESS"
                        }
                        startService(intent)
                        
                        val broadcastIntent = Intent("com.noxscreen.app.BIOMETRIC_SUCCESS")
                        sendBroadcast(broadcastIntent)
                    } else if (authTarget == "APP_LOCK") {
                        val broadcastIntent = Intent("com.noxscreen.app.APP_LOCK_UNLOCKED")
                        sendBroadcast(broadcastIntent)
                    }

                    setResult(Activity.RESULT_OK)
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    vibrateError()
                    val isLocked = securityManager.recordFailedAttempt()
                    if (isLocked) {
                        Toast.makeText(
                            applicationContext, 
                            "3 isku-day oo khaldan! App-ka waa la xannibay 30 ilbiriqsi.", 
                            Toast.LENGTH_LONG
                        ).show()
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    } else {
                        Toast.makeText(
                            applicationContext, 
                            "Xaqiijintu waa fashilantay. Isku day mar kale.", 
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })

        val title = if (authTarget == "APP_LOCK") "NoxScreen App Lock" else "Unlock NoxScreen"
        val subtitle = if (authTarget == "APP_LOCK") "Xaqiiji fartaada ama furaha taleefanka si aad u gasho app-ka" else "Use your biometric or device lock to unlock"

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun vibrateError() {
        try {
            val vibrator = getSystemService(Vibrator::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(120)
            }
        } catch (e: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isSuccess && authTarget == "BLACKOUT") {
            val intent = Intent(this, BlackScreenService::class.java).apply {
                action = "BIOMETRIC_FAILED"
            }
            startService(intent)
        }
    }
}
