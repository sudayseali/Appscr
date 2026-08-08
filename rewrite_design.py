import re

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "r") as f:
    content = f.read()

# 1. Update colors
colors_replacement = """val ZenithBackgroundStart = Color(0xFF00050A)
val ZenithBackgroundEnd = Color(0xFF0A0F1A)
val ZenithCard = Color(0xFF131722)
val ZenithAccent = Color(0xFF00E676) // Bright Green
val ZenithSecondary = Color(0xFF7B61FF) // Soft Purple
val ZenithTextMuted = Color(0xFF8B92A5)"""

content = re.sub(
    r'val ZenithBackground = Color.*?val ZenithGradientEnd = Color\(0xFF111424\)',
    colors_replacement,
    content,
    flags=re.DOTALL
)

# Replace ZenithApp layout
target_zenith_app = r'fun ZenithApp\(.*?}\n    }\n}'
zenith_app_replacement = """fun ZenithApp(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    isServiceRunning: Boolean,
    totalTimeSaved: Long,
    usageCount: Int,
    onUnlockPremiumStyle: (String, () -> Unit, () -> Unit, () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val automationSettings = remember { com.noxscreen.app.automation.AutomationSettings(context) }
    var autoConfig by remember { mutableStateOf(automationSettings.getConfig()) }
    val scrollState = rememberScrollState()
    
    var showAdLoading by remember { mutableStateOf(false) }
    
    val estimatedMah = ((totalTimeSaved / (1000f * 60f * 60f)) * 200f).toInt()
    val formattedTime = String.format("%dh %dm", totalTimeSaved / (1000 * 60 * 60), (totalTimeSaved / (1000 * 60)) % 60)
    
    Box(modifier = Modifier.fillMaxSize().background(
        brush = Brush.verticalGradient(listOf(ZenithBackgroundStart, ZenithBackgroundEnd))
    )) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 48.dp, bottom = 120.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("NoxScreen", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text(" Pro", color = ZenithAccent, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("ECO SCREEN OPTIMIZER", color = ZenithTextMuted, fontSize = 11.sp, letterSpacing = 2.sp, modifier = Modifier.padding(top = 4.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Security, contentDescription = "Protected", tint = ZenithAccent, modifier = Modifier.size(24.dp))
                    Text("Protected", color = ZenithTextMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Central Power Button
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(Brush.radialGradient(listOf(ZenithAccent.copy(alpha = 0.2f), Color.Transparent)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .border(2.dp, Brush.sweepGradient(listOf(ZenithAccent, Color.Transparent, ZenithAccent)), CircleShape)
                        .background(Color(0xFF0A0F1A), CircleShape)
                        .clickable {
                            if (!hasPermission) onRequestPermission()
                            else if (isServiceRunning) onStopService()
                            else onStartService()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PowerSettingsNew, 
                        contentDescription = "Power", 
                        tint = if (isServiceRunning) Color.White else ZenithAccent,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(if (isServiceRunning) "Tap to wake screen" else "Tap to sleep screen", color = ZenithAccent, fontSize = 14.sp)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Stats Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    title = "Energy Saved",
                    value = "${estimatedMah} mAh",
                    icon = Icons.Default.Bolt,
                    color = ZenithAccent,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Screen Off",
                    value = formattedTime,
                    icon = Icons.Default.Schedule,
                    color = ZenithSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Settings Rows (Visual only for now, replacing ExpandableConfigSections with these cards)
            SettingsMenuCard(
                title = "Display Settings",
                subtitle = "Customize how the screen behaves",
                icon = Icons.Default.DisplaySettings,
                iconColor = ZenithAccent,
                badgeText = "3 Active",
                badgeColor = ZenithAccent
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SettingsMenuCard(
                title = "Smart Triggers",
                subtitle = "Auto actions based on your movements",
                icon = Icons.Default.Sensors,
                iconColor = ZenithSecondary,
                badgeText = "4 Active",
                badgeColor = ZenithSecondary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SettingsMenuCard(
                title = "Security",
                subtitle = "Protect your app and privacy",
                icon = Icons.Default.Security,
                iconColor = Color(0xFF2196F3),
                badgeText = "Biometric Off",
                badgeColor = Color(0xFF2196F3)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SettingsMenuCard(
                title = "Focus Mode",
                subtitle = "Limit usage and stay productive",
                icon = Icons.Default.GpsFixed,
                iconColor = Color(0xFFFF9800),
                badgeText = "Limits Off",
                badgeColor = Color(0xFFFF9800)
            )
            
            Spacer(modifier = Modifier.height(120.dp))
        }
        
        // Bottom controls
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DarkMode, contentDescription = "Dark Mode", tint = Color.White, modifier = Modifier.size(24.dp))
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Brush.radialGradient(listOf(ZenithAccent.copy(alpha=0.3f), Color.Transparent)), CircleShape)
                                .border(1.dp, ZenithAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock", tint = ZenithAccent)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.KeyboardDoubleArrowLeft, contentDescription = null, tint = ZenithAccent.copy(alpha=0.5f), modifier = Modifier.size(16.dp))
                            Text(" Drag to move ", color = ZenithAccent, fontSize = 12.sp)
                            Icon(Icons.Default.KeyboardDoubleArrowRight, contentDescription = null, tint = ZenithAccent.copy(alpha=0.5f), modifier = Modifier.size(16.dp))
                        }
                    }
                    
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(24.dp))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .background(ZenithCard, RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Eco, contentDescription = "Eco", tint = ZenithAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Thank you for saving energy and", color = ZenithTextMuted, fontSize = 12.sp)
                        Text("extending your screen life.", color = ZenithTextMuted, fontSize = 12.sp)
                    }
                    Icon(Icons.Default.Favorite, contentDescription = "Heart", tint = ZenithAccent, modifier = Modifier.size(24.dp))
                }
            }
        }
        
        if (!hasPermission) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.8f)).padding(24.dp), contentAlignment = Alignment.Center) {
                PermissionBanner(onRequestPermission)
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(ZenithCard, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = ZenithTextMuted, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        // Mock graph
        Row(modifier = Modifier.fillMaxWidth().height(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            for (i in 0..15) {
                val h = if (i % 3 == 0) 12 else if (i % 2 == 0) 8 else 4
                Box(modifier = Modifier.width(3.dp).height(h.dp).background(color.copy(alpha = 0.7f), RoundedCornerShape(1.dp)))
            }
        }
    }
}

@Composable
fun SettingsMenuCard(title: String, subtitle: String, icon: ImageVector, iconColor: Color, badgeText: String, badgeColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ZenithCard, RoundedCornerShape(16.dp))
            .clickable { /* Expand logic */ }
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
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ZenithTextMuted, modifier = Modifier.size(20.dp))
    }
}
"""

# Let's replace ZenithApp first.
# ZenithApp starts with `fun ZenithApp(` and goes until its closing brace.
import re
content = re.sub(r'fun ZenithApp\(.*?}\n    }\n}', zenith_app_replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "w") as f:
    f.write(content)

