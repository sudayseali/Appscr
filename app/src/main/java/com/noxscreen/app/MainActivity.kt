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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.noxscreen.app.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    var hasPermission by remember { mutableStateOf(checkOverlayPermission()) }
                    
                    val context = LocalContext.current
                    val prefs = remember { context.getSharedPreferences("BlackScreenStats", Context.MODE_PRIVATE) }
                    var totalTimeSaved by remember { mutableStateOf(prefs.getLong("total_time_saved", 0L)) }
                    var usageCount by remember { mutableStateOf(prefs.getInt("usage_count", 0)) }
                    var hasRated by remember { mutableStateOf(prefs.getBoolean("has_rated", false)) }
                    var showRatingDialog by remember { mutableStateOf(false) }
                    
                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                hasPermission = checkOverlayPermission()
                                totalTimeSaved = prefs.getLong("total_time_saved", 0L)
                                usageCount = prefs.getInt("usage_count", 0)
                                if (usageCount >= 3 && !hasRated && !showRatingDialog) {
                                    showRatingDialog = true
                                }
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    if (showRatingDialog) {
                        AlertDialog(
                            onDismissRequest = { 
                                showRatingDialog = false
                                prefs.edit().putBoolean("has_rated", true).apply()
                                hasRated = true
                            },
                            title = { Text("Enjoying NoxScreen Pro?") },
                            text = { Text("If this app helps you save battery, please rate us on the Play Store to help us grow!") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showRatingDialog = false
                                    prefs.edit().putBoolean("has_rated", true).apply()
                                    hasRated = true
                                }) {
                                    Text("Rate Now")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showRatingDialog = false
                                    prefs.edit().putBoolean("has_rated", true).apply()
                                    hasRated = true
                                }) {
                                    Text("Maybe Later")
                                }
                            }
                        )
                    }

                    BlackScreenApp(
                        hasPermission = hasPermission,
                        onRequestPermission = { requestOverlayPermission() },
                        onStartService = { startBlackScreenService() },
                        onShareApp = { shareApp() },
                        totalTimeSaved = totalTimeSaved
                    )
                }
            }
        }
    }

    private fun shareApp() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Save your battery while listening to videos! Download NoxScreen Pro: https://play.google.com/store/apps/details?id=$packageName")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
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
            startActivity(intent)
        }
    }

    private fun startBlackScreenService() {
        if (!checkOverlayPermission()) {
            requestOverlayPermission()
            return
        }
        val intent = Intent(this, BlackScreenService::class.java).apply {
            action = "START_BLACKOUT"
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
        }
    }
}

@Composable
fun BlackScreenApp(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartService: () -> Unit,
    onShareApp: () -> Unit,
    totalTimeSaved: Long
) {
    val bgBlack = Color(0xFF000000)
    val slate900 = Color(0xFF0F172A)
    val slate800 = Color(0xFF1E293B)
    val slate300 = Color(0xFFCBD5E1)
    val slate400 = Color(0xFF94A3B8)
    val slate500 = Color(0xFF64748B)
    val blue400 = Color(0xFF60A5FA)
    val blue500 = Color(0xFF3B82F6)
    val amber500 = Color(0xFFF59E0B)
    val emerald400 = Color(0xFF34D399)

    val context = LocalContext.current
    val automationSettings = remember { com.noxscreen.app.automation.AutomationSettings(context) }
    val analyticsManager = remember { com.noxscreen.app.automation.UsageAnalyticsManager(context) }

    var autoConfig by remember { mutableStateOf(automationSettings.getConfig()) }
    var suggestionText by remember { mutableStateOf(analyticsManager.getSuggestedAutomation()) }

    val estimatedMah = ((totalTimeSaved / (1000f * 60f * 60f)) * 200f).toInt()
    val scrollState = androidx.compose.foundation.rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBlack)
            .padding(top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                Text("NoxScreen ", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp)
                Text("Pro", color = blue400, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp)
            }
            IconButton(
                onClick = onShareApp,
                modifier = Modifier
                    .size(40.dp)
                    .background(slate900, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share App",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (!hasPermission) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = blue400,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 16.dp)
                )
                Text(
                    text = "Permission Required",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "NoxScreen Pro needs the 'Display over other apps' permission to blackout your screen while videos play.",
                    color = slate400,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { onRequestPermission() },
                    colors = ButtonDefaults.buttonColors(containerColor = blue500),
                    shape = CircleShape
                ) {
                    Text("Grant Permission", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Main UI
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // The Master Trigger
                Box(
                    modifier = Modifier.size(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Glow
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(blue500.copy(alpha = 0.15f), Color.Transparent)
                                )
                            )
                    )
                    
                    // Button
                    Button(
                        onClick = { onStartService() },
                        modifier = Modifier.size(180.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = slate900),
                        border = androidx.compose.foundation.BorderStroke(4.dp, slate800),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start",
                                tint = blue400,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "START", 
                                color = slate300, 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 14.sp,
                                letterSpacing = 4.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // Stats Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Battery Saved Stat
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(slate900.copy(alpha = 0.6f), shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                            .border(1.dp, slate800, shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Battery Saved",
                            tint = blue400,
                            modifier = Modifier.size(20.dp).padding(bottom = 4.dp)
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$estimatedMah",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            )
                            Text(
                                text = "mAh",
                                color = slate500,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                        }
                        Text(
                            text = "SAVED ALL TIME",
                            color = slate400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Timer Stat
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(slate900.copy(alpha = 0.6f), shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                            .border(1.dp, slate800, shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Timer Status",
                            tint = emerald400,
                            modifier = Modifier.size(20.dp).padding(bottom = 4.dp)
                        )
                        Text(
                            text = if (autoConfig.isTimerEnabled) "${autoConfig.timerDurationSeconds}s" else "OFF",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        )
                        Text(
                            text = if (autoConfig.isTimerEnabled) "TIMER DELAY" else "TIMER: OFF",
                            color = slate400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))

                // Smart Automation Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(slate900.copy(alpha = 0.6f), shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                        .border(1.dp, slate800, shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "SMART AUTOMATION",
                        color = blue400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Suggestion Banner if applicable
                    suggestionText?.let { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(blue500.copy(alpha = 0.15f), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                .border(1.dp, blue500.copy(alpha = 0.3f), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = suggestion,
                                color = slate300,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    automationSettings.setPocketModeEnabled(true)
                                    autoConfig = automationSettings.getConfig()
                                    suggestionText = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = blue500),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                            ) {
                                Text("Enable", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Feature 1: Timer Toggle & Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Activation Delay Timer", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Delay blackout activation", color = slate400, fontSize = 11.sp)
                        }
                        Switch(
                            checked = autoConfig.isTimerEnabled,
                            onCheckedChange = { checked ->
                                automationSettings.setTimerEnabled(checked)
                                autoConfig = automationSettings.getConfig()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = blue500)
                        )
                    }

                    if (autoConfig.isTimerEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(5, 10, 30, 60).forEach { sec ->
                                val label = if (sec == 60) "1m" else "${sec}s"
                                val isSelected = autoConfig.timerDurationSeconds == sec
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        automationSettings.setTimerDuration(sec)
                                        autoConfig = automationSettings.getConfig()
                                    },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = blue500,
                                        selectedLabelColor = Color.White,
                                        containerColor = slate800,
                                        labelColor = slate300
                                    ),
                                    modifier = Modifier.height(32.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = slate800)

                    // Feature 2: Proximity / Pocket Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pocket / Proximity Mode", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Auto-blackout when in pocket or covered", color = slate400, fontSize = 11.sp)
                        }
                        Switch(
                            checked = autoConfig.isPocketModeEnabled,
                            onCheckedChange = { checked ->
                                automationSettings.setPocketModeEnabled(checked)
                                autoConfig = automationSettings.getConfig()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = blue500)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = slate800)

                    // Feature 3: Motion Detection Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stationary Motion Mode", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Auto-blackout when device is stationary", color = slate400, fontSize = 11.sp)
                        }
                        Switch(
                            checked = autoConfig.isMotionDetectionEnabled,
                            onCheckedChange = { checked ->
                                automationSettings.setMotionDetectionEnabled(checked)
                                autoConfig = automationSettings.getConfig()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = blue500)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Monetization Placeholder (Native Ad style)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(slate900.copy(alpha = 0.4f), shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                        .border(1.dp, slate800.copy(alpha = 0.3f), shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(blue500, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Premium",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "AD", 
                                color = amber500, 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(amber500.copy(alpha = 0.2f), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                            Text("Sponsored", color = slate500, fontSize = 10.sp)
                        }
                        Text(
                            "Unlock Auto-Stop Timer 30 min",
                            color = slate300,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { /* Unlock */ },
                        colors = ButtonDefaults.buttonColors(containerColor = blue500),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("GET", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
