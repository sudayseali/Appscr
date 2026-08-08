import re

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "r") as f:
    content = f.read()

# 1. Update usages
content = content.replace(
"""            ExpandableConfigSection(
                title = stringResource(R.string.display_settings),
                icon = Icons.Default.DisplaySettings,
                isExpanded = true
            ) {""",
"""            ExpandableConfigSection(
                title = stringResource(R.string.display_settings),
                subtitle = "Customize how the screen behaves",
                icon = Icons.Default.DisplaySettings,
                iconColor = ZenithAccent,
                badgeText = "3 Active",
                badgeColor = ZenithAccent,
                isExpanded = true
            ) {"""
)

content = content.replace(
"""            ExpandableConfigSection(
                title = stringResource(R.string.smart_triggers),
                icon = Icons.Default.Sensors,
                isExpanded = false
            ) {""",
"""            ExpandableConfigSection(
                title = stringResource(R.string.smart_triggers),
                subtitle = "Auto actions based on your movements",
                icon = Icons.Default.Sensors,
                iconColor = ZenithSecondary,
                badgeText = "4 Active",
                badgeColor = ZenithSecondary,
                isExpanded = false
            ) {"""
)

content = content.replace(
"""            ExpandableConfigSection(
                title = stringResource(R.string.security),
                icon = Icons.Default.Lock,
                isExpanded = false
            ) {""",
"""            ExpandableConfigSection(
                title = stringResource(R.string.security),
                subtitle = "Protect your app and privacy",
                icon = Icons.Default.Security,
                iconColor = Color(0xFF2196F3),
                badgeText = "Biometric Off",
                badgeColor = Color(0xFF2196F3),
                isExpanded = false
            ) {"""
)

content = content.replace(
"""            ExpandableConfigSection(
                title = stringResource(R.string.usage_limits),
                icon = Icons.Default.HealthAndSafety,
                isExpanded = false
            ) {""",
"""            ExpandableConfigSection(
                title = "Focus Mode",
                subtitle = "Limit usage and stay productive",
                icon = Icons.Default.GpsFixed,
                iconColor = Color(0xFFFF9800),
                badgeText = "Limits Off",
                badgeColor = Color(0xFFFF9800),
                isExpanded = false
            ) {"""
)

# 2. Update definition
target_def = r'fun ExpandableConfigSection\(title: String, icon: ImageVector, isExpanded: Boolean, content: @Composable ColumnScope\.\(\) -> Unit\) \{.*?\n\s*var expanded by remember'

replacement_def = """fun ExpandableConfigSection(title: String, subtitle: String, icon: ImageVector, iconColor: Color, badgeText: String, badgeColor: Color, isExpanded: Boolean, content: @Composable ColumnScope.() -> Unit) {
    var expanded by remember"""

content = re.sub(target_def, replacement_def, content, flags=re.DOTALL)

# 3. Update internal layout of ExpandableConfigSection
target_layout = r'Column\(\s*modifier = Modifier\s*\.fillMaxWidth\(\)\s*\.clip\(RoundedCornerShape\(24\.dp\)\)\s*\.background\(Brush\.verticalGradient\(listOf\(ZenithCard, Color\(0x80131422\)\)\)\)\.border\(1\.dp, Color\.White\.copy\(alpha=0\.05f\), RoundedCornerShape\(20\.dp\)\)\s*\.border\(1\.dp, Color\.White\.copy\(alpha = 0\.05f\), RoundedCornerShape\(24\.dp\)\)\s*\.padding\(vertical = 8\.dp\)\s*\) \{.*?AnimatedVisibility\(visible = expanded\)'

replacement_layout = """Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ZenithCard)
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = ZenithTextMuted, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(badgeText, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ChevronRight,
                contentDescription = null,
                tint = ZenithTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
        
        AnimatedVisibility(visible = expanded)"""

content = re.sub(target_layout, replacement_layout, content, flags=re.DOTALL)

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "w") as f:
    f.write(content)
