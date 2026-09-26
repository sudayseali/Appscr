package com.noxscreen.app.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators

/**
 * AppSecurityManager
 *
 * Maamusha xaaladda amniga ee qalabka:
 * 1. Hubinta awoodda biometric (Fingerprint / Face Unlock / Device PIN).
 * 2. Ka-hortagga isku-dayga khaldan (Intruder Lockout / Max attempt throttling).
 * 3. Taageerada furaha noocyada kala duwan leh.
 */
class AppSecurityManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "NoxAppSecurity"
        private const val MAX_FAILED_ATTEMPTS = 3
        private const val LOCKOUT_DURATION_MS = 30_000L // 30 ilbiriqsi
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    enum class BiometricStatus {
        AVAILABLE,
        NONE_ENROLLED,
        NOT_SUPPORTED,
        HARDWARE_UNAVAILABLE
    }

    /**
     * Returns the valid BiometricManager authenticator bitmask for the current Android API level.
     * On API 24-29 (Android 7-10), BIOMETRIC_STRONG | DEVICE_CREDENTIAL is rejected by AndroidX Biometric 1.1.0,
     * whereas BIOMETRIC_WEAK | DEVICE_CREDENTIAL is supported across API 24-36 and also supports Class 2 Face/Fingerprint.
     */
    fun getSupportedAuthenticators(): Int {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL
        } else {
            Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL
        }
    }

    /**
     * Hubi in qalabku leeyahay Biometric ama Device Credential (PIN/Pattern/Password) diyaar ah.
     */
    fun checkBiometricAvailability(): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        val status = biometricManager.canAuthenticate(getSupportedAuthenticators())
        return when (status) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
                if (keyguardManager?.isDeviceSecure == true) {
                    BiometricStatus.AVAILABLE
                } else {
                    BiometricStatus.NONE_ENROLLED
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
                if (keyguardManager?.isDeviceSecure == true) {
                    BiometricStatus.AVAILABLE
                } else {
                    BiometricStatus.NOT_SUPPORTED
                }
            }
            else -> {
                val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
                if (keyguardManager?.isDeviceSecure == true) {
                    BiometricStatus.AVAILABLE
                } else {
                    BiometricStatus.HARDWARE_UNAVAILABLE
                }
            }
        }
    }

    /**
     * Hubi in isticmaalaha hadda laga xannibay furista sababtoo ah isku-dayyo khaldan oo badan.
     */
    fun isLockedOut(): Boolean {
        val lockoutEndTime = prefs.getLong("lockout_until", 0L)
        val now = System.currentTimeMillis()
        return now < lockoutEndTime
    }

    /**
     * Soo celi inta ilbiriqsi ee ka dhiman xannibaadda.
     */
    fun getRemainingLockoutSeconds(): Int {
        val lockoutEndTime = prefs.getLong("lockout_until", 0L)
        val now = System.currentTimeMillis()
        val diff = lockoutEndTime - now
        return if (diff > 0) (diff / 1000).toInt() else 0
    }

    /**
     * Diiwaangeli isku-day khaldan.
     * Haddii ay gaarto 3 jeer, xannib 30 ilbiriqsi.
     */
    fun recordFailedAttempt(): Boolean {
        val currentFailed = prefs.getInt("failed_attempts", 0) + 1
        if (currentFailed >= MAX_FAILED_ATTEMPTS) {
            val lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            prefs.edit()
                .putInt("failed_attempts", 0)
                .putLong("lockout_until", lockoutUntil)
                .apply()
            return true // Hadda ayaa la xannibay
        } else {
            prefs.edit()
                .putInt("failed_attempts", currentFailed)
                .apply()
            return false
        }
    }

    /**
     * Dib u deji xisaabiyaha markii si guul leh loo furo.
     */
    fun recordSuccess() {
        prefs.edit()
            .putInt("failed_attempts", 0)
            .putLong("lockout_until", 0L)
            .apply()
    }
}
