package com.noxscreen.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.noxscreen.app.ui.theme.MyApplicationTheme
import com.noxscreen.app.automation.AutomationConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF050510)
                ) {
                    var hasPermission by remember { mutableStateOf(checkOverlayPermission()) }
                    
                    val context = LocalContext.current
                    val prefs = remember { context.getSharedPreferences("BlackScreenStats", Context.MODE_PRIVATE) }
                    var totalTimeSaved by remember { mutableStateOf(prefs.getLong("total_time_saved", 0L)) }
                    var usageCount by remember { mutableStateOf(prefs.getInt("usage_count", 0)) }

                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                hasPermission = checkOverlayPermission()
                                totalTimeSaved = prefs.getLong("total_time_saved", 0L)
                                usageCount = prefs.getInt("usage_count", 0)
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    var isServiceRunning by remember { mutableStateOf(isServiceRunning()) }
                    
                    LaunchedEffect(Unit) {
                        while(true) {
                            isServiceRunning = isServiceRunning()
                            kotlinx.coroutines.delay(1000)
                        }
                    }

                    ZenithApp(
                        hasPermission = hasPermission,
                        onRequestPermission = { requestOverlayPermission() },
                        onStartService = { 
                            startBlackScreenService()
                            isServiceRunning = true
                        },
                        onStopService = {
                            stopBlackScreenService()
                            isServiceRunning = false
                        },
                        isServiceRunning = isServiceRunning,
                        totalTimeSaved = totalTimeSaved,
                        usageCount = usageCount
                    )
                }
            }
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            try {
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (BlackScreenService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }
    
    private fun stopBlackScreenService() {
        val intent = Intent(this, BlackScreenService::class.java).apply {
            action = "STOP_SERVICE"
        }
        startService(intent)
    }

    private fun startBlackScreenService() {
        if (!checkOverlayPermission()) {
            // Even if we don't have overlay permission, we can launch BlackoutActivity!
            val intent = Intent(this, BlackoutActivity::class.java)
            startActivity(intent)
            return
        }
        val intent = Intent(this, BlackScreenService::class.java).apply {
            action = "START_SERVICE"
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            moveTaskToBack(true)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to BlackoutActivity
            val fallbackIntent = Intent(this, BlackoutActivity::class.java)
            startActivity(fallbackIntent)
        }
    }
}

// Custom Unique Theme Colors for Zenith
val ZenithBackground = Color(0xFF030A16)
val ZenithCard = Color(0xFF091429)
val ZenithAccent = Color(0xFF00FFB2) // Neon Aqua Green
val ZenithSecondary = Color(0xFF8C9EFF) // Soft Blue
val ZenithTextMuted = Color(0xFF7A8DAB)

@Composable
fun ZenithApp(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    isServiceRunning: Boolean,
    totalTimeSaved: Long,
    usageCount: Int
) {
    val context = LocalContext.current
    val automationSettings = remember { com.noxscreen.app.automation.AutomationSettings(context) }
    var autoConfig by remember { mutableStateOf(automationSettings.getConfig()) }
    val scrollState = rememberScrollState()
    
    val estimatedMah = ((totalTimeSaved / (1000f * 60f * 60f)) * 200f).toInt()
    
    Box(modifier = Modifier.fillMaxSize().background(ZenithBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 64.dp, bottom = 120.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text("ZENITH", color = ZenithAccent, fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = 8.sp)
            Text("ECO SCREEN OPTIMIZER", color = ZenithSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Central Power Button
            PowerPulseButton(
                onClick = {
                    if (isServiceRunning) onStopService() else onStartService()
                },
                isRunning = isServiceRunning
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            if (!hasPermission) {
                PermissionBanner(onRequestPermission)
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Unique Impact Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ImpactCard("Energy Saved", "${estimatedMah} mAh", Icons.Default.EnergySavingsLeaf, ZenithAccent, Modifier.weight(1f))
                val hours = (totalTimeSaved / 3600000).toInt()
                val mins = ((totalTimeSaved % 3600000) / 60000).toInt()
                ImpactCard("Screen Off", "${hours}h ${mins}m", Icons.Default.Timer, ZenithSecondary, Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Expandable Settings
            ExpandableConfigSection(
                title = "Display Settings",
                icon = Icons.Default.DisplaySettings,
                isExpanded = true
            ) {
                ZenithSwitchRow("Always-On Display", "Show time on dark screen", autoConfig.isAodEnabled) { 
                    autoConfig = autoConfig.copy(isAodEnabled = it); automationSettings.updateConfig(autoConfig) 
                }
                ZenithSwitchRow("OLED Pixel Shift", "Prevent screen burn-in", autoConfig.oledBurnInProtection) { 
                    autoConfig = autoConfig.copy(oledBurnInProtection = it); automationSettings.updateConfig(autoConfig) 
                }
                ZenithSwitchRow("Privacy Tint", "Dim screen instead of total black", autoConfig.isDarkTintEnabled) { 
                    autoConfig = autoConfig.copy(isDarkTintEnabled = it); automationSettings.updateConfig(autoConfig) 
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ExpandableConfigSection(
                title = "Smart Triggers",
                icon = Icons.Default.Sensors,
                isExpanded = false
            ) {
                ZenithSwitchRow("Pocket Mode", "Auto-lock in pocket", autoConfig.isPocketModeEnabled) { 
                    autoConfig = autoConfig.copy(isPocketModeEnabled = it); automationSettings.updateConfig(autoConfig) 
                }
                ZenithSwitchRow("Floating Action Button", "Show quick-access button", !autoConfig.hideFloatingButton) { 
                    autoConfig = autoConfig.copy(hideFloatingButton = !it); automationSettings.updateConfig(autoConfig) 
                }
                
                
                Text("Floating Lock Style", color = ZenithSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                
                val styles = listOf("lock" to androidx.compose.ui.res.painterResource(R.drawable.ic_lock),
                    "moon" to androidx.compose.ui.res.painterResource(R.drawable.ic_moon),
                    "circle" to androidx.compose.ui.res.painterResource(R.drawable.ic_circle),
                    "double_circle" to androidx.compose.ui.res.painterResource(R.drawable.ic_double_circle),
                    "key" to androidx.compose.ui.res.painterResource(R.drawable.ic_key),
                    "eye_off" to androidx.compose.ui.res.painterResource(R.drawable.ic_eye_off))
                        
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(styles.size) { index ->
                        val (styleName, painter) = styles[index]
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ZenithCard)
                                .border(
                                    2.dp,
                                    if (autoConfig.floatingLockStyle == styleName) ZenithAccent else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { 
                                    autoConfig = autoConfig.copy(floatingLockStyle = styleName)
                                    automationSettings.updateConfig(autoConfig) 
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(painter, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                
                Text("Floating Lock Size", color = ZenithSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp))
                androidx.compose.material3.Slider(
                    value = autoConfig.floatingLockSize,
                    onValueChange = { 
                        autoConfig = autoConfig.copy(floatingLockSize = it)
                        automationSettings.updateConfig(autoConfig)
                    },
                    valueRange = 0.5f..2.0f,
                    colors = androidx.compose.material3.SliderDefaults.colors(
                        thumbColor = ZenithAccent,
                        activeTrackColor = ZenithAccent,
                        inactiveTrackColor = ZenithCard
                    )
                )

                // Wake gesture selector
                Text("Wake Gesture (Taps)", color = ZenithSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    (1..4).forEach { taps ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (autoConfig.tapsToWake == taps) ZenithAccent else ZenithCard)
                                .clickable { autoConfig = autoConfig.copy(tapsToWake = taps); automationSettings.updateConfig(autoConfig) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$taps", color = if (autoConfig.tapsToWake == taps) ZenithBackground else Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ExpandableConfigSection(
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
                icon = Icons.Default.HealthAndSafety,
                isExpanded = false
            ) {
                Text("Helpful for digital wellbeing", color = ZenithTextMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                ZenithSwitchRow("Daily Limit (1hr)", "Stop overlay after limit", false) { }
                ZenithSwitchRow("Bedtime Mode", "Auto-trigger at 10 PM", false) { }
            }
        }
    }
}

@Composable
fun PowerPulseButton(onClick: () -> Unit, isRunning: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(140.dp)
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                spotColor = ZenithAccent.copy(alpha = glow)
            )
            .background(ZenithBackground, CircleShape)
            .border(2.dp, ZenithAccent.copy(alpha = 0.5f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Inner pulsing ring
        Box(
            modifier = Modifier
                .size(100.dp * pulse)
                .background(ZenithAccent.copy(alpha = 0.15f), CircleShape)
        )
        // Core button
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(
                    brush = Brush.radialGradient(listOf(ZenithCard, ZenithBackground)),
                    shape = CircleShape
                )
                .border(1.dp, ZenithAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PowerSettingsNew, contentDescription = "Start", tint = ZenithAccent, modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
fun PermissionBanner(onRequestPermission: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF331414))
            .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .clickable(onClick = onRequestPermission),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Overlay Permission Required", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Tap to grant access for background functionality.", color = Color(0xFFFFB3B3), fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFFF5252))
    }
}

@Composable
fun ImpactCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ZenithCard)
            .padding(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, color = ZenithTextMuted, fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ExpandableConfigSection(title: String, icon: ImageVector, isExpanded: Boolean, content: @Composable ColumnScope.() -> Unit) {
    var expanded by remember { mutableStateOf(isExpanded) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ZenithCard)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = ZenithSecondary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = ZenithTextMuted
            )
        }
        
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun ZenithSwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, color = ZenithTextMuted, fontSize = 11.sp, lineHeight = 14.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ZenithBackground,
                checkedTrackColor = ZenithAccent,
                uncheckedThumbColor = ZenithTextMuted,
                uncheckedTrackColor = ZenithBackground,
                uncheckedBorderColor = ZenithTextMuted
            )
        )
    }
}
