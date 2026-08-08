import os

code = """package com.noxscreen.app

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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Analytics
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
import androidx.compose.ui.res.stringResource
import com.noxscreen.app.R

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.noxscreen.app.ui.theme.MyApplicationTheme
import com.noxscreen.app.automation.AutomationConfig

class MainActivity : ComponentActivity() {
    private lateinit var adsManager: com.noxscreen.app.ads.UnityAdsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("BlackScreenStats", Context.MODE_PRIVATE)
        val savedLang = prefs.getString("app_language", "en") ?: "en"
        @Suppress("DEPRECATION")
        val locale = java.util.Locale(savedLang)
        java.util.Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
        
        adsManager = com.noxscreen.app.ads.UnityAdsManager(this)
        adsManager.initialize()

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF020612)
                ) {
                    var showSplash by remember { mutableStateOf(true) }
                    
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(2200)
                        showSplash = false
                    }
                    
                    if (showSplash) {
                        SplashScreen()
                    } else {
                        var hasPermission by remember { mutableStateOf(checkOverlayPermission()) }
                        var hasRequestedPermissionOnStart by remember { mutableStateOf(false) }
                        
                        LaunchedEffect(hasPermission) {
                            if (!hasPermission && !hasRequestedPermissionOnStart) {
                                hasRequestedPermissionOnStart = true
                                requestOverlayPermission()
                            }
                        }
                    
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
                                adsManager.onStopAction(this@MainActivity)
                            },
                            isServiceRunning = isServiceRunning,
                            totalTimeSaved = totalTimeSaved,
                            usageCount = usageCount,
                            onUnlockPremiumStyle = { styleName, onLoading, onSuccess, onFailed ->
                                adsManager.showRewardedAdWithWait(
                                    this@MainActivity,
                                    onLoading = onLoading,
                                    onSuccess = {
                                        val currentConfig = com.noxscreen.app.automation.AutomationSettings(this@MainActivity).getConfig()
                                        val newUnlocked = currentConfig.unlockedStyles + styleName
                                        val newConfig = currentConfig.copy(floatingLockStyle = styleName, unlockedStyles = newUnlocked)
                                        com.noxscreen.app.automation.AutomationSettings(this@MainActivity).updateConfig(newConfig)
                                        onSuccess()
                                    },
                                    onFailed = onFailed
                                )
                            }
                        )
                    }
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
        }
    }
}

// Dark Luxury Premium Theme Colors for NoxScreen Pro
val ZenithBackgroundStart = Color(0xFF020612)
val ZenithBackgroundEnd = Color(0xFF091122)
val ZenithCard = Color(0xFF0F172A)
val ZenithCardGlow = Color(0xFF16233B)
val ZenithCardBorder = Color(0xFF1E293B)
val ZenithAccent = Color(0xFF00E676) // Glowing Emerald
val ZenithAccentGlow = Color(0xFF00FF88)
val ZenithSecondary = Color(0xFF7C4DFF) // Neon Violet
val ZenithCyan = Color(0xFF00E5FF) // Cyber Cyan
val ZenithTextMuted = Color(0xFF94A3B8)

@Composable
fun ZenithApp(
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
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ZenithBackgroundStart, ZenithBackgroundEnd, Color(0xFF030712))
                )
            )
    ) {
        // Ambient Radial Background Light Spot
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-60).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isServiceRunning) ZenithAccent.copy(alpha = 0.18f) else ZenithCyan.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 52.dp, bottom = 140.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Premium Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "NoxScreen",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(ZenithAccent, ZenithCyan)
                                    )
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PRO",
                                color = Color(0xFF020612),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.eco_screen_optimizer).uppercase(),
                        color = ZenithTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Protected Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(ZenithAccent.copy(alpha = 0.12f))
                        .border(1.dp, ZenithAccent.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (isServiceRunning) ZenithAccent else Color(0xFFFFB74D), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isServiceRunning) "ACTIVE" else "PROTECTED",
                            color = if (isServiceRunning) ZenithAccent else Color(0xFFFFB74D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Central Power Core Button
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                (if (isServiceRunning) ZenithAccent else ZenithCyan).copy(alpha = 0.22f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                PowerPulseButton(
                    onClick = {
                        if (!hasPermission) {
                            onRequestPermission()
                        } else if (isServiceRunning) {
                            onStopService()
                        } else {
                            onStartService()
                        }
                    },
                    isRunning = isServiceRunning
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isServiceRunning) "Tap to wake screen" else "Tap to sleep screen",
                color = if (isServiceRunning) ZenithAccent else ZenithCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!hasPermission) {
                PermissionBanner(onRequestPermission)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Impact Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ImpactCard(
                    title = stringResource(R.string.energy_saved),
                    value = "${estimatedMah} mAh",
                    icon = Icons.Default.EnergySavingsLeaf,
                    color = ZenithAccent,
                    barIndex = 0,
                    modifier = Modifier.weight(1f)
                )
                val hours = (totalTimeSaved / 3600000).toInt()
                val mins = ((totalTimeSaved % 3600000) / 60000).toInt()
                ImpactCard(
                    title = stringResource(R.string.screen_off),
                    value = "${hours}h ${mins}m",
                    icon = Icons.Default.Timer,
                    color = ZenithSecondary,
                    barIndex = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gamification Level Section
            GamificationSection(totalTimeSaved = totalTimeSaved)

            Spacer(modifier = Modifier.height(24.dp))

            // Expandable Settings Cards
            ExpandableConfigSection(
                title = stringResource(R.string.display_settings),
                subtitle = "Customize screen behaviour & lock style",
                icon = Icons.Default.DisplaySettings,
                iconColor = ZenithAccent,
                badgeText = "3 Active",
                badgeColor = ZenithAccent,
                isExpanded = true
            ) {
                LanguageRow()

                ZenithSwitchRow(
                    title = stringResource(R.string.always_on_display),
                    subtitle = "Show clock & subtle notifications on dark screen",
                    checked = autoConfig.isAodEnabled
                ) {
                    autoConfig = autoConfig.copy(isAodEnabled = it)
                    automationSettings.updateConfig(autoConfig)
                }

                ZenithSwitchRow(
                    title = stringResource(R.string.oled_pixel_shift),
                    subtitle = "Prevent screen burn-in with micro shifts",
                    checked = autoConfig.oledBurnInProtection
                ) {
                    autoConfig = autoConfig.copy(oledBurnInProtection = it)
                    automationSettings.updateConfig(autoConfig)
                }

                ZenithSwitchRow(
                    title = stringResource(R.string.skip_unlock_screen),
                    subtitle = "Directly unlock device on tap gesture",
                    checked = autoConfig.isSkipUnlockScreenEnabled
                ) {
                    autoConfig = autoConfig.copy(isSkipUnlockScreenEnabled = it)
                    automationSettings.updateConfig(autoConfig)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExpandableConfigSection(
                title = stringResource(R.string.smart_triggers),
                subtitle = "Auto actions based on motion sensors",
                icon = Icons.Default.Sensors,
                iconColor = ZenithSecondary,
                badgeText = "4 Active",
                badgeColor = ZenithSecondary,
                isExpanded = false
            ) {
                ZenithSwitchRow(
                    title = stringResource(R.string.pocket_mode),
                    subtitle = "Auto-lock screen when device placed in pocket",
                    checked = autoConfig.isPocketModeEnabled
                ) {
                    autoConfig = autoConfig.copy(isPocketModeEnabled = it)
                    automationSettings.updateConfig(autoConfig)
                }

                ZenithSwitchRow(
                    title = stringResource(R.string.flip_to_sleep),
                    subtitle = "Turn screen face down to activate sleep mode",
                    checked = autoConfig.isFlipToSleepEnabled
                ) {
                    autoConfig = autoConfig.copy(isFlipToSleepEnabled = it)
                    automationSettings.updateConfig(autoConfig)
                }

                ZenithSwitchRow(
                    title = stringResource(R.string.shake_to_wake),
                    subtitle = "Shake device firmly to unlock screen",
                    checked = autoConfig.isShakeToWakeEnabled
                ) {
                    autoConfig = autoConfig.copy(isShakeToWakeEnabled = it)
                    automationSettings.updateConfig(autoConfig)
                }

                ZenithSwitchRow(
                    title = stringResource(R.string.floating_action_button),
                    subtitle = "Show quick-access floating trigger lock",
                    checked = !autoConfig.hideFloatingButton
                ) {
                    autoConfig = autoConfig.copy(hideFloatingButton = !it)
                    automationSettings.updateConfig(autoConfig)
                }

                Text(
                    text = stringResource(R.string.floating_lock_style),
                    color = ZenithSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
                )

                val styles = listOf(
                    "lock" to androidx.compose.ui.res.painterResource(R.drawable.ic_lock),
                    "moon" to androidx.compose.ui.res.painterResource(R.drawable.ic_moon),
                    "circle" to androidx.compose.ui.res.painterResource(R.drawable.ic_circle),
                    "double_circle" to androidx.compose.ui.res.painterResource(R.drawable.ic_double_circle),
                    "key" to androidx.compose.ui.res.painterResource(R.drawable.ic_key),
                    "eye_off" to androidx.compose.ui.res.painterResource(R.drawable.ic_eye_off),
                    "shield" to androidx.compose.ui.res.painterResource(R.drawable.ic_shield),
                    "fingerprint" to androidx.compose.ui.res.painterResource(R.drawable.ic_fingerprint),
                    "power" to androidx.compose.ui.res.painterResource(R.drawable.ic_power),
                    "bolt" to androidx.compose.ui.res.painterResource(R.drawable.ic_bolt),
                    "favorite" to androidx.compose.ui.res.painterResource(R.drawable.ic_favorite)
                )

                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(styles.size) { index ->
                        val (styleName, painter) = styles[index]
                        val isUnlocked = autoConfig.unlockedStyles.contains(styleName)
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val isSelected = autoConfig.floatingLockStyle == styleName
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) ZenithAccent.copy(alpha = 0.15f) else ZenithCardGlow
                                )
                                .border(
                                    1.5.dp,
                                    if (isSelected) ZenithAccent else ZenithCardBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    if (isUnlocked) {
                                        autoConfig = autoConfig.copy(floatingLockStyle = styleName)
                                        automationSettings.updateConfig(autoConfig)
                                    } else {
                                        onUnlockPremiumStyle(
                                            styleName,
                                            { showAdLoading = true },
                                            {
                                                showAdLoading = false
                                                autoConfig = automationSettings.getConfig()
                                            },
                                            {
                                                showAdLoading = false
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Connect to internet to unlock premium icon.",
                                                    android.widget.Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painter,
                                contentDescription = null,
                                tint = if (isSelected) ZenithAccent else if (isUnlocked) Color.White else Color.White.copy(alpha = 0.35f),
                                modifier = Modifier.size(24.dp)
                            )
                            if (!isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = ZenithAccent,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .align(Alignment.BottomEnd)
                                        .padding(bottom = 4.dp, end = 4.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.floating_lock_size),
                    color = ZenithSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Slider(
                    value = autoConfig.floatingLockSize,
                    onValueChange = {
                        autoConfig = autoConfig.copy(floatingLockSize = it)
                        automationSettings.updateConfig(autoConfig)
                    },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = ZenithAccent,
                        activeTrackColor = ZenithAccent,
                        inactiveTrackColor = ZenithCardBorder
                    )
                )

                Text(
                    text = stringResource(R.string.wake_gesture),
                    color = ZenithSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    (1..4).forEach { taps ->
                        val isSelected = autoConfig.tapsToWake == taps
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ZenithAccent else ZenithCardGlow)
                                .border(1.dp, if (isSelected) ZenithAccent else ZenithCardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    autoConfig = autoConfig.copy(tapsToWake = taps)
                                    automationSettings.updateConfig(autoConfig)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$taps ${if (taps == 1) "Tap" else "Taps"}",
                                color = if (isSelected) Color(0xFF020612) else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExpandableConfigSection(
                title = stringResource(R.string.security),
                subtitle = "Protect app access & privacy controls",
                icon = Icons.Default.Security,
                iconColor = ZenithCyan,
                badgeText = "Biometric Off",
                badgeColor = ZenithCyan,
                isExpanded = false
            ) {
                ZenithSwitchRow(
                    title = stringResource(R.string.enable_biometric),
                    subtitle = "Use fingerprint or face recognition to access app settings",
                    checked = autoConfig.isBiometricEnabled
                ) {
                    autoConfig = autoConfig.copy(isBiometricEnabled = it)
                    automationSettings.updateConfig(autoConfig)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExpandableConfigSection(
                title = "Focus Mode",
                subtitle = "App limits & distraction controls",
                icon = Icons.Default.GpsFixed,
                iconColor = Color(0xFFFF9800),
                badgeText = "Limits Off",
                badgeColor = Color(0xFFFF9800),
                isExpanded = false
            ) {
                val lifecycleOwner = LocalLifecycleOwner.current
                var hasUsageStatsPermission by remember { mutableStateOf(false) }

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            hasUsageStatsPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
                                val mode = appOps.checkOpNoThrow(
                                    android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                                    android.os.Process.myUid(),
                                    context.packageName
                                )
                                mode == android.app.AppOpsManager.MODE_ALLOWED
                            } else {
                                true
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                if (!hasUsageStatsPermission) {
                    Button(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZenithAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text("Grant Usage Access", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    ZenithSwitchRow(
                        title = "Enable App Limits",
                        subtitle = "Lock distraction apps when limit is reached",
                        checked = autoConfig.isUsageLimitsEnabled
                    ) {
                        autoConfig = autoConfig.copy(isUsageLimitsEnabled = it)
                        automationSettings.updateConfig(autoConfig)
                    }
                    if (autoConfig.isUsageLimitsEnabled) {
                        Text(
                            text = "Limit: ${autoConfig.usageLimitDurationMinutes} minutes",
                            color = ZenithSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                        Slider(
                            value = autoConfig.usageLimitDurationMinutes.toFloat(),
                            onValueChange = {
                                autoConfig = autoConfig.copy(usageLimitDurationMinutes = it.toInt())
                                automationSettings.updateConfig(autoConfig)
                            },
                            valueRange = 1f..120f,
                            steps = 118,
                            colors = SliderDefaults.colors(
                                thumbColor = ZenithAccent,
                                activeTrackColor = ZenithAccent,
                                inactiveTrackColor = ZenithCardBorder
                            )
                        )

                        Button(
                            onClick = {
                                val newBlockedApps = if (autoConfig.blockedApps.isEmpty()) {
                                    setOf("com.google.android.youtube", "com.android.chrome", "com.facebook.katana", "com.instagram.android", "com.zhiliaoapp.musically", "com.snapchat.android", "com.whatsapp", "com.twitter.android", "com.google.android.apps.photos", "com.sec.android.gallery3d", "com.android.gallery3d")
                                } else {
                                    emptySet<String>()
                                }
                                autoConfig = autoConfig.copy(blockedApps = newBlockedApps)
                                automationSettings.updateConfig(autoConfig)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ZenithSecondary.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text(
                                text = if (autoConfig.blockedApps.isEmpty()) "Block Distraction Apps" else "Unblock All Apps",
                                color = ZenithAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (autoConfig.blockedApps.isNotEmpty()) {
                            Text(
                                text = "Blocked: ${autoConfig.blockedApps.size} apps (Socials, Video & Gallery)",
                                color = ZenithTextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Dock Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quick Control Floating Dock
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ZenithCard)
                        .border(1.dp, ZenithCardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = "Dark Mode",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Drag Trigger Indicator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(ZenithAccent.copy(alpha = 0.3f), Color.Transparent)
                                ),
                                CircleShape
                            )
                            .border(1.5.dp, ZenithAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Trigger",
                            tint = ZenithAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.KeyboardDoubleArrowLeft,
                            contentDescription = null,
                            tint = ZenithAccent.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = " Drag to move ",
                            color = ZenithAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardDoubleArrowRight,
                            contentDescription = null,
                            tint = ZenithAccent.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ZenithCard)
                        .border(1.dp, ZenithCardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Eco Thank You Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ZenithCard)
                    .border(1.dp, ZenithCardBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(ZenithAccent.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = "Eco",
                        tint = ZenithAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Thank you for saving energy",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Extending your OLED screen life.",
                        color = ZenithTextMuted,
                        fontSize = 11.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Heart",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            com.noxscreen.app.ads.UnityBannerAd(
                adUnitId = "Banner_Android",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PowerPulseButton(onClick: () -> Unit, isRunning: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(150.dp)
            .shadow(
                elevation = 24.dp,
                shape = CircleShape,
                spotColor = (if (isRunning) ZenithAccent else ZenithCyan).copy(alpha = glow)
            )
            .background(
                brush = Brush.radialGradient(
                    listOf(ZenithBackgroundStart, ZenithBackgroundEnd)
                ),
                shape = CircleShape
            )
            .border(
                2.dp,
                Brush.linearGradient(
                    listOf(if (isRunning) ZenithAccent else ZenithCyan, ZenithSecondary)
                ),
                CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Inner pulsing ring
        Box(
            modifier = Modifier
                .size(110.dp * pulse)
                .background((if (isRunning) ZenithAccent else ZenithCyan).copy(alpha = 0.15f), CircleShape)
        )
        // Core button
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(ZenithCardGlow, ZenithBackgroundEnd)
                    ),
                    shape = CircleShape
                )
                .border(1.5.dp, if (isRunning) ZenithAccent else ZenithCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = "Power Action",
                tint = if (isRunning) ZenithAccent else ZenithCyan,
                modifier = Modifier.size(44.dp)
            )
        }
    }
}

@Composable
fun PermissionBanner(onRequestPermission: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2C1010))
            .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .clickable(onClick = onRequestPermission),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFFF5252),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.overlay_permission),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.tap_to_grant),
                color = Color(0xFFFFB3B3),
                fontSize = 12.sp
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFFF5252)
        )
    }
}

@Composable
fun ImpactCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    barIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(ZenithCard, Color(0xFF09111F))
                )
            )
            .border(1.dp, ZenithCardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = ZenithTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        // Mini Sparkline Graph
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            val heights = if (barIndex == 0) listOf(4, 6, 8, 5, 10, 14, 8, 12, 16, 10, 18, 12, 14, 20)
                          else listOf(6, 8, 5, 12, 10, 8, 14, 12, 18, 16, 12, 14, 18, 20)
            heights.forEach { h ->
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(h.dp)
                        .background(color.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@Composable
fun ExpandableConfigSection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    badgeText: String,
    badgeColor: Color,
    isExpanded: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(isExpanded) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ZenithCard)
            .border(1.dp, ZenithCardBorder, RoundedCornerShape(20.dp))
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
                    .size(44.dp)
                    .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = ZenithTextMuted,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
            Box(
                modifier = Modifier
                    .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    color = badgeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ChevronRight,
                contentDescription = null,
                tint = ZenithTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
        
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                HorizontalDivider(color = ZenithCardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                content()
            }
        }
    }
}

@Composable
fun GamificationSection(totalTimeSaved: Long) {
    val totalHours = (totalTimeSaved / 3600000).toFloat()
    
    val (level, currentLevelThreshold, nextLevelThreshold) = when {
        totalHours < 1 -> Triple(0, 0f, 1f)
        totalHours < 5 -> Triple(1, 1f, 5f)
        totalHours < 10 -> Triple(2, 5f, 10f)
        totalHours < 25 -> Triple(3, 10f, 25f)
        totalHours < 50 -> Triple(4, 25f, 50f)
        totalHours < 100 -> Triple(5, 50f, 100f)
        else -> Triple(6, 100f, 200f)
    }
    
    val progress = ((totalHours - currentLevelThreshold) / (nextLevelThreshold - currentLevelThreshold)).coerceIn(0f, 1f)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(ZenithCard, Color(0xFF0D1525))
                )
            )
            .border(1.dp, ZenithCardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Level $level",
                        color = ZenithAccent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(ZenithSecondary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (level == 0) "ECO NOVICE" else "ECO SAVER",
                            color = ZenithSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "${String.format("%.1f", totalHours)} Hours Saved Total",
                    color = ZenithTextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFFFD700).copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = if (level > 0) Color(0xFFFFD700) else Color.Gray,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = ZenithAccent,
            trackColor = ZenithCardBorder
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${currentLevelThreshold.toInt()}h",
                color = ZenithTextMuted,
                fontSize = 11.sp
            )
            Text(
                text = "${(nextLevelThreshold - totalHours).coerceAtLeast(0f).toInt()}h to next level",
                color = ZenithTextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun ZenithSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    color = ZenithTextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ZenithAccent,
                uncheckedThumbColor = ZenithTextMuted,
                uncheckedTrackColor = Color(0xFF1E293B),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(ZenithBackgroundStart, ZenithBackgroundEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(ZenithAccent.copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, ZenithAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NightsStay,
                    contentDescription = "App Icon",
                    tint = ZenithAccent,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "NOXSCREEN",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp
            )
            Text(
                text = "ECO SCREEN OPTIMIZER",
                color = ZenithAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(
                color = ZenithAccent,
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )
        }
    }
}

@Composable
fun LanguageRow() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    
    val languages = listOf(
        "en" to "English",
        "so" to "Somali",
        "ar" to "العربية",
        "bn" to "বাংলা",
        "zh" to "中文",
        "es" to "Español",
        "fr" to "Français",
        "de" to "Deutsch",
        "hi" to "हिन्दी",
        "id" to "Bahasa Indonesia",
        "it" to "Italiano",
        "ja" to "日本語",
        "ko" to "한국어",
        "mr" to "मराठी",
        "pa" to "ਪੰਜਾਬੀ",
        "pt" to "Português",
        "ru" to "Русский",
        "te" to "తెలుగు",
        "tr" to "Türkçe",
        "ur" to "اردو",
        "vi" to "Tiếng Việt",
        "sw" to "Kiswahili",
        "fa" to "فارسی",
        "ta" to "தமிழ்",
        "gu" to "ગુજરાતી"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable { expanded = true },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = stringResource(R.string.language),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.select_language),
                color = ZenithTextMuted,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        
        Box {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(ZenithSecondary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Language",
                    tint = ZenithSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(ZenithCard)
            ) {
                languages.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = { Text(name, color = Color.White, fontSize = 13.sp) },
                        onClick = {
                            expanded = false
                            setAppLocale(context, code)
                        }
                    )
                }
            }
        }
    }
}

fun setAppLocale(context: Context, languageCode: String) {
    val prefs = context.getSharedPreferences("BlackScreenStats", Context.MODE_PRIVATE)
    prefs.edit().putString("app_language", languageCode).apply()
    
    @Suppress("DEPRECATION")
    val locale = java.util.Locale(languageCode)
    java.util.Locale.setDefault(locale)
    val resources = context.resources
    val config = resources.configuration
    config.setLocale(locale)
    @Suppress("DEPRECATION")
    resources.updateConfiguration(config, resources.displayMetrics)
    if (context is android.app.Activity) {
        context.recreate()
    }
}
"""

target_path = "app/src/main/java/com/noxscreen/app/MainActivity.kt"
with open(target_path, "w") as f:
    f.write(code)

print(f"{target_path} written successfully!")
