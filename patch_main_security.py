import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """            ExpandableConfigSection(
                title = "Usage Limits",
                icon = Icons.Default.HealthAndSafety,"""

replacement = """            ExpandableConfigSection(
                title = "Security",
                icon = Icons.Default.Lock,
                isExpanded = false
            ) {
                ZenithSwitchRow("Enable Biometric Authentication", "Use fingerprint or face recognition to enhance security", autoConfig.isBiometricEnabled) { 
                    autoConfig = autoConfig.copy(isBiometricEnabled = it); automationSettings.updateConfig(autoConfig) 
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ExpandableConfigSection(
                title = "Usage Limits",
                icon = Icons.Default.HealthAndSafety,"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
    f.write(content)
