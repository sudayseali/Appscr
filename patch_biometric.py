import sys

with open('app/src/main/java/com/noxscreen/app/BiometricAuthActivity.kt', 'r') as f:
    content = f.read()

target = """        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock NoxScreen")
            .setSubtitle("Use your fingerprint or face to unlock")
            .setNegativeButtonText("Cancel")
            .build()"""

replacement = """        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock NoxScreen")
            .setSubtitle("Use your biometric or device lock to unlock")
            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/BiometricAuthActivity.kt', 'w') as f:
        f.write(content)
    print("Replaced!")
else:
    print("Not found")
