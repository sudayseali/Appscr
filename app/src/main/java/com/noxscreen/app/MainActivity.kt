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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import com.noxscreen.app.R

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noxscreen.app.ui.theme.MyApplicationTheme
import com.noxscreen.app.ui.theme.*
import com.noxscreen.app.automation.AutomationConfig

class MainActivity : ComponentActivity() {
    private lateinit var adsManager: com.noxscreen.app.ads.UnityAdsManager
    private val authLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != android.app.Activity.RESULT_OK) {
            finish()
        }
    }


    override fun onResume() {
        super.onResume()
        if (!com.noxscreen.app.security.AuthenticationManager.isAuthenticated(this)) {
            com.noxscreen.app.security.AuthenticationManager.setAuthenticating()
            val intent = android.content.Intent(this, BiometricAuthActivity::class.java)
            authLauncher.launch(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.noxscreen.app.automation.FloatingLockEntitlementManager(this).validateActiveStyle()
        
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
                    var isAuthenticated by androidx.compose.runtime.remember { 
                        androidx.compose.runtime.mutableStateOf(com.noxscreen.app.security.AuthenticationManager.isAuthenticated(this@MainActivity)) 
                    }
                    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                                isAuthenticated = com.noxscreen.app.security.AuthenticationManager.isAuthenticated(this@MainActivity)
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    if (!isAuthenticated) {
                        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                        return@Surface
                    }

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

                        val isServiceRunning by BlackScreenService.isRunningFlow.collectAsStateWithLifecycle(initialValue = BlackScreenService.isRunning)

                        ZenithApp(
                            hasPermission = hasPermission,
                            onRequestPermission = { requestOverlayPermission() },
                            onStartService = { 
                                startBlackScreenService()
                            },
                            onStopService = {
                                stopBlackScreenService()
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
                                        com.noxscreen.app.automation.FloatingLockEntitlementManager(this@MainActivity).grantUnlock(styleName)
                                        val currentConfig = com.noxscreen.app.automation.AutomationSettings(this@MainActivity).getConfig()
                                        val newConfig = currentConfig.copy(floatingLockStyle = styleName)
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
// Colors moved to Color.kt

@Composable
fun ZenithApp(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    isServiceRunning: Boolean,
    totalTimeSaved: Long,
    usageCount: Int,
    onUnlockPremiumStyle: (String, () -> Unit, () -> Unit, (String) -> Unit) -> (() -> Unit)?
) {
    val context = LocalContext.current
    val automationSettings = remember { com.noxscreen.app.automation.AutomationSettings(context) }
    var autoConfig by remember { mutableStateOf(automationSettings.getConfig()) }
    val scrollState = rememberScrollState()
    
    var showAdLoading by remember { mutableStateOf(false) }
    var cancelAdLoad by remember { mutableStateOf<(() -> Unit)?>(null) }
    val estimatedMah = ((totalTimeSaved / (1000f * 60f * 60f)) * 200f).toInt()
    
    if (showAdLoading) {
        com.noxscreen.app.ui.LoadingAdDialog(
            onDismissRequest = {
                cancelAdLoad?.invoke()
                showAdLoading = false
            }
        )
    }
    
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
                .fillMaxHeight()
                .widthIn(max = 600.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(scrollState)
                .padding(top = 52.dp, bottom = 140.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Premium Header Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1324)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1C2D4A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NoxScreen",
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF00E5FF), Color(0xFF00E676))
                                        )
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    color = Color(0xFF020612),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.eco_screen_optimizer).uppercase(),
                            color = Color(0xFF94A3B8),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Protected / Active Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isServiceRunning) Color(0xFF00E676).copy(alpha = 0.16f) else Color(0xFFFFB300).copy(alpha = 0.16f)
                            )
                            .border(
                                1.dp,
                                if (isServiceRunning) Color(0xFF00E676).copy(alpha = 0.4f) else Color(0xFFFFB300).copy(alpha = 0.4f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (isServiceRunning) Color(0xFF00E676) else Color(0xFFFFB300), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isServiceRunning) "ACTIVE" else "PROTECTED",
                                color = if (isServiceRunning) Color(0xFF00E676) else Color(0xFFFFB300),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Central Power Core Button & Floating Action Button Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                // Central Start Power Button
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
                    isRunning = isServiceRunning,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Floating Action Button Quick Toggle (placed right next to start button)
                val isFloatingOn = !autoConfig.hideFloatingButton
                val floatBgColor by animateColorAsState(
                    targetValue = if (isFloatingOn) ZenithAccent.copy(alpha = 0.15f) else ZenithCardGlow,
                    animationSpec = tween(300)
                )
                val floatBorderColor by animateColorAsState(
                    targetValue = if (isFloatingOn) ZenithAccent else ZenithCardBorder,
                    animationSpec = tween(300)
                )
                val floatIconTint by animateColorAsState(
                    targetValue = if (isFloatingOn) ZenithAccent else ZenithTextMuted,
                    animationSpec = tween(300)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 4.dp, bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(floatBgColor)
                            .border(1.5.dp, floatBorderColor, CircleShape)
                            .clickable {
                                autoConfig = autoConfig.copy(hideFloatingButton = !autoConfig.hideFloatingButton)
                                automationSettings.updateConfig(autoConfig)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_bolt),
                            contentDescription = "Toggle Floating Overlay Widget",
                            tint = floatIconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Widget",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isFloatingOn) "ON" else "OFF",
                        color = floatIconTint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isServiceRunning) "Tap to wake screen" else "Tap to sleep screen",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            EqualizerWaveBar(
                isRunning = isServiceRunning,
                color = if (isServiceRunning) ZenithAccent else ZenithCyan
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
                    isRunning = isServiceRunning,
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
                    isRunning = isServiceRunning,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gamification Level Section
            GamificationSection(totalTimeSaved = totalTimeSaved)

            Spacer(modifier = Modifier.height(24.dp))

            // Expandable Settings Cards
            ExpandableConfigSection(
                title = "Screen & Display",
                subtitle = "Clock, always-on display & pixel shift",
                icon = Icons.Default.DisplaySettings,
                iconColor = ZenithAccent,
                badgeText = "Display",
                badgeColor = ZenithAccent,
                isExpanded = false
            ) {
SmartTriggerCard(
                    title = stringResource(R.string.always_on_display),
                    subtitle = "Show clock & subtle notifications on dark screen",
                    icon = Icons.Default.Schedule,
                    iconTint = Color(0xFF00E676),
                    checked = autoConfig.isAodEnabled,
                    onCheckedChange = {
                        autoConfig = autoConfig.copy(isAodEnabled = it)
                        automationSettings.updateConfig(autoConfig)
                    }
                ) {
                    Column {
                        AodGraphic()
                        if (autoConfig.isAodEnabled) {
                            com.noxscreen.app.ui.ClockStyleSelector(
                                selectedStyle = autoConfig.clockStyle,
                                onStyleSelected = { newStyle ->
                                    autoConfig = autoConfig.copy(clockStyle = newStyle)
                                    automationSettings.updateConfig(autoConfig)
                                }
                            )
                            com.noxscreen.app.ui.AodThemeSelector(
                                selectedTheme = autoConfig.aodThemeColor,
                                onThemeSelected = { newTheme ->
                                    autoConfig = autoConfig.copy(aodThemeColor = newTheme)
                                    automationSettings.updateConfig(autoConfig)
                                }
                            )
                        }
                    }
                }


SmartTriggerCard(
                    title = stringResource(R.string.oled_pixel_shift),
                    subtitle = "Prevent screen burn-in with micro shifts",
                    icon = Icons.Default.Grain,
                    iconTint = Color(0xFFFFB300),
                    checked = autoConfig.oledBurnInProtection,
                    onCheckedChange = {
                        autoConfig = autoConfig.copy(oledBurnInProtection = it)
                        automationSettings.updateConfig(autoConfig)
                    }
                ) {
                    OledPixelShiftGraphic()
                }


            }

            Spacer(modifier = Modifier.height(16.dp))

            ExpandableConfigSection(
                title = "Controls & Overlays",
                subtitle = "Unlock methods & floating buttons",
                icon = Icons.Default.TouchApp,
                iconColor = Color(0xFFAB47BC),
                badgeText = "Controls",
                badgeColor = Color(0xFFAB47BC),
                isExpanded = false
            ) {
SmartTriggerCard(
                    title = stringResource(R.string.skip_unlock_screen),
                    subtitle = "Directly unlock device on tap gesture",
                    icon = Icons.Default.LockOpen,
                    iconTint = Color(0xFFAB47BC),
                    checked = autoConfig.isSkipUnlockScreenEnabled,
                    onCheckedChange = {
                        autoConfig = autoConfig.copy(isSkipUnlockScreenEnabled = it)
                        automationSettings.updateConfig(autoConfig)
                    }
                ) {
                    Column {
                        SkipUnlockGraphic()
                        if (!autoConfig.isSkipUnlockScreenEnabled) {
                            com.noxscreen.app.ui.UnlockScreenStyleSelector(
                                selectedStyle = autoConfig.unlockScreenStyle,
                                onStyleSelected = { newStyle ->
                                    autoConfig = autoConfig.copy(unlockScreenStyle = newStyle)
                                    automationSettings.updateConfig(autoConfig)
                                }
                            )
                        }
                    }
                }

SmartTriggerCard(
                    title = stringResource(R.string.floating_action_button),
                    subtitle = "Quick access button",
                    icon = Icons.Default.TouchApp,
                    iconTint = Color(0xFFFFB300),
                    checked = !autoConfig.hideFloatingButton,
                    onCheckedChange = {
                        autoConfig = autoConfig.copy(hideFloatingButton = !it)
                        automationSettings.updateConfig(autoConfig)
                    }
                ) {
                    FloatingButtonGraphic()
                }


                Text(
                    text = stringResource(R.string.floating_lock_style).uppercase(),
                    color = ZenithCyan,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(top = 18.dp, bottom = 12.dp)
                )

                val styles = listOf(
                    "lock" to androidx.compose.ui.res.painterResource(R.drawable.ic_lock),
                    "moon" to androidx.compose.ui.res.painterResource(R.drawable.ic_moon),
                    "circle" to androidx.compose.ui.res.painterResource(R.drawable.ic_circle),
                    "double_circle" to androidx.compose.ui.res.painterResource(R.drawable.ic_double_circle),
                    "crown" to androidx.compose.ui.res.painterResource(R.drawable.ic_crown),
                    "diamond" to androidx.compose.ui.res.painterResource(R.drawable.ic_diamond),
                    "star" to androidx.compose.ui.res.painterResource(R.drawable.ic_star),
                    "fire" to androidx.compose.ui.res.painterResource(R.drawable.ic_fire),
                    "atom" to androidx.compose.ui.res.painterResource(R.drawable.ic_atom),
                    "shield_lock" to androidx.compose.ui.res.painterResource(R.drawable.ic_shield_lock),
                    "key" to androidx.compose.ui.res.painterResource(R.drawable.ic_key),
                    "eye_off" to androidx.compose.ui.res.painterResource(R.drawable.ic_eye_off),
                    "shield" to androidx.compose.ui.res.painterResource(R.drawable.ic_shield),
                    "fingerprint" to androidx.compose.ui.res.painterResource(R.drawable.ic_fingerprint),
                    "power" to androidx.compose.ui.res.painterResource(R.drawable.ic_power),
                    "bolt" to androidx.compose.ui.res.painterResource(R.drawable.ic_bolt),
                    "favorite" to androidx.compose.ui.res.painterResource(R.drawable.ic_favorite)
                )
                val entitlementManager = remember { com.noxscreen.app.automation.FloatingLockEntitlementManager(context) }
                var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
                LaunchedEffect(Unit) {
                    entitlementManager.migrateOldStylesIfNeeded()
                    while(true) {
                        kotlinx.coroutines.delay(60000)
                        currentTime = System.currentTimeMillis()
                    }
                }
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(styles.size) { index ->
                        val (styleName, painter) = styles[index]
                        val isUnlocked = entitlementManager.isStyleUnlocked(styleName)
                        val remainingTime = entitlementManager.getFormattedRemainingTime(styleName)
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val isSelected = autoConfig.floatingLockStyle == styleName
                        
                        Column(
                            modifier = Modifier
                                .width(120.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (isSelected) Color(0xFF00E676).copy(alpha = 0.12f) else Color(0xFF0B1426)
                                )
                                .border(
                                    width = if (isSelected) 1.8.dp else 1.dp,
                                    color = if (isSelected) ZenithAccent else Color(0xFF1E2D47),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable {
                                    if (isUnlocked) {
                                        autoConfig = autoConfig.copy(floatingLockStyle = styleName)
                                        automationSettings.updateConfig(autoConfig)
                                    } else {
                                        cancelAdLoad = onUnlockPremiumStyle(
                                            styleName,
                                            { showAdLoading = true },
                                            {
                                                showAdLoading = false
                                                entitlementManager.grantUnlock(styleName)
                                                autoConfig = automationSettings.getConfig()
                                                currentTime = System.currentTimeMillis()
                                            },
                                            { errorMsg ->
                                                showAdLoading = false
                                                android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                }
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(
                                        if (isSelected) ZenithAccent.copy(alpha = 0.18f) else Color(0xFF131D31),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painter,
                                    contentDescription = null,
                                    tint = if (isSelected) ZenithAccent else if (isUnlocked) Color.White else Color.White.copy(alpha = 0.35f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val displayName = styleName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString() }.replace("_", " ")
                            Text(
                                text = displayName,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .background(ZenithAccent.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .border(1.dp, ZenithAccent.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ACTIVE", color = ZenithAccent, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                                }
                            } else if (isUnlocked) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = ZenithAccent, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Unlocked", color = ZenithAccent, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                                }
                                if (remainingTime != null) {
                                    Text(remainingTime, color = ZenithTextMuted, fontSize = 9.5.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 2.dp))
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Locked", color = Color(0xFFFFB300), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Text("Watch Ad", color = ZenithCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }

                var liveFloatingLockSize by remember(autoConfig.floatingLockSize) { mutableFloatStateOf(autoConfig.floatingLockSize) }

                LaunchedEffect(liveFloatingLockSize) {
                    if (liveFloatingLockSize != autoConfig.floatingLockSize) {
                        kotlinx.coroutines.delay(400)
                        autoConfig = autoConfig.copy(floatingLockSize = liveFloatingLockSize)
                        automationSettings.updateConfig(autoConfig)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.floating_lock_size).uppercase(),
                        color = ZenithCyan,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "${(liveFloatingLockSize * 100).toInt()}%",
                        color = ZenithAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = liveFloatingLockSize,
                    onValueChange = {
                        liveFloatingLockSize = it
                    },
                    onValueChangeFinished = {
                        if (autoConfig.floatingLockSize != liveFloatingLockSize) {
                            autoConfig = autoConfig.copy(floatingLockSize = liveFloatingLockSize)
                            automationSettings.updateConfig(autoConfig)
                        }
                    },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = ZenithAccent,
                        activeTrackColor = ZenithAccent,
                        inactiveTrackColor = Color(0xFF1E293B)
                    )
                )

            }

            Spacer(modifier = Modifier.height(16.dp))

            ExpandableConfigSection(
                title = "Automation (Sensors)",
                subtitle = "Pocket mode & shake gestures",
                icon = Icons.Default.Sensors,
                iconColor = ZenithSecondary,
                badgeText = "Sensors",
                badgeColor = ZenithSecondary,
                isExpanded = false
            ) {
SmartTriggerCard(
                    title = stringResource(R.string.pocket_mode),
                    subtitle = "Auto-lock in pocket",
                    icon = Icons.Default.Smartphone,
                    iconTint = Color(0xFF00E676),
                    checked = autoConfig.isPocketModeEnabled,
                    onCheckedChange = {
                        autoConfig = autoConfig.copy(isPocketModeEnabled = it)
                        automationSettings.updateConfig(autoConfig)
                    }
                ) {
                    PocketModeWaveGraphic()
                }

SmartTriggerCard(
                    title = stringResource(R.string.shake_to_wake),
                    subtitle = "Shake to unlock",
                    icon = Icons.Default.Vibration,
                    iconTint = Color(0xFF00E5FF),
                    checked = autoConfig.isShakeToWakeEnabled,
                    onCheckedChange = {
                        autoConfig = autoConfig.copy(isShakeToWakeEnabled = it)
                        automationSettings.updateConfig(autoConfig)
                    }
                ) {
                    ShakeToWakeGraphic()
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.wake_gesture).uppercase(),
                        color = ZenithCyan,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0C1322))
                        .border(1.dp, Color(0xFF1B2C46), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Taps to Wake",
                                color = Color.White,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Number of screen taps to illuminate",
                                color = ZenithTextMuted,
                                fontSize = 11.5.sp
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFF131D31), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF1E2F4D), RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            IconButton(
                                onClick = { 
                                    if (autoConfig.tapsToWake > 1) {
                                        autoConfig = autoConfig.copy(tapsToWake = autoConfig.tapsToWake - 1)
                                        automationSettings.updateConfig(autoConfig)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease taps", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = autoConfig.tapsToWake.toString(),
                                color = ZenithCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            IconButton(
                                onClick = { 
                                    if (autoConfig.tapsToWake < 10) {
                                        autoConfig = autoConfig.copy(tapsToWake = autoConfig.tapsToWake + 1)
                                        automationSettings.updateConfig(autoConfig)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase taps", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
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
                badgeText = "Biometric",
                badgeColor = ZenithCyan,
                isExpanded = false
            ) {
ZenithSwitchRow(
                    title = stringResource(R.string.enable_biometric),
                    subtitle = "Require fingerprint to unlock screen and stop service",
                    checked = autoConfig.isBiometricEnabled
                ) {
                    autoConfig = autoConfig.copy(isBiometricEnabled = it)
                    automationSettings.updateConfig(autoConfig)
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            ExpandableConfigSection(
                title = "General",
                subtitle = "App language and preferences",
                icon = Icons.Default.Settings,
                iconColor = Color.Gray,
                badgeText = "General",
                badgeColor = Color.Gray,
                isExpanded = false
            ) {
                LanguageCard()

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
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text("Grant Usage Access", color = Color(0xFF030712), fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                    }
                } else {
                    
                    ZenithSwitchRow(
                        title = "Enable Schedule Limits",
                        subtitle = "Lock distraction apps during scheduled times",
                        checked = autoConfig.isScheduleEnabled
                    ) {
                        autoConfig = autoConfig.copy(isScheduleEnabled = it)
                        automationSettings.updateConfig(autoConfig)
                    }

                    if (autoConfig.isScheduleEnabled) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Start Time Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF0C1322))
                                    .border(1.dp, Color(0xFF1B2C46), RoundedCornerShape(14.dp))
                                    .clickable {
                                        android.app.TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                autoConfig = autoConfig.copy(scheduleStartTimeHour = hour, scheduleStartTimeMinute = minute)
                                                automationSettings.updateConfig(autoConfig)
                                            },
                                            autoConfig.scheduleStartTimeHour,
                                            autoConfig.scheduleStartTimeMinute,
                                            true
                                        ).show()
                                    }
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text("START TIME", color = ZenithTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = ZenithCyan, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = String.format("%02d:%02d", autoConfig.scheduleStartTimeHour, autoConfig.scheduleStartTimeMinute),
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // End Time Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF0C1322))
                                    .border(1.dp, Color(0xFF1B2C46), RoundedCornerShape(14.dp))
                                    .clickable {
                                        android.app.TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                autoConfig = autoConfig.copy(scheduleEndTimeHour = hour, scheduleEndTimeMinute = minute)
                                                automationSettings.updateConfig(autoConfig)
                                            },
                                            autoConfig.scheduleEndTimeHour,
                                            autoConfig.scheduleEndTimeMinute,
                                            true
                                        ).show()
                                    }
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text("END TIME", color = ZenithTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = ZenithSecondary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = String.format("%02d:%02d", autoConfig.scheduleEndTimeHour, autoConfig.scheduleEndTimeMinute),
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (autoConfig.isScheduleEnabled) {
                        var showAppSelection by remember { mutableStateOf(false) }

                        if (showAppSelection) {
                            com.noxscreen.app.ui.AppSelectionDialog(
                                initialSelectedApps = autoConfig.blockedApps,
                                onDismissRequest = { showAppSelection = false },
                                onAppsSelected = { apps ->
                                    autoConfig = autoConfig.copy(blockedApps = apps)
                                    automationSettings.updateConfig(autoConfig)
                                    showAppSelection = false
                                }
                            )
                        }

                        Button(
                            onClick = { showAppSelection = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF142238)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ZenithCyan.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Apps, contentDescription = null, tint = ZenithCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (autoConfig.blockedApps.isNotEmpty()) "Select Apps (${autoConfig.blockedApps.size} Selected)" else "Select Apps to Block",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (autoConfig.blockedApps.isNotEmpty()) {
                            Text(
                                text = "Active Shield: ${autoConfig.blockedApps.size} applications blocked during session",
                                color = ZenithTextMuted,
                                fontSize = 11.5.sp,
                                modifier = Modifier.padding(top = 6.dp, start = 2.dp)
                            )
                        }
                    }

                }
            }
        }
        // Bottom Banner Ad
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            com.noxscreen.app.ads.UnityBannerAd(
                adUnitId = "Banner_Android",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PowerPulseButton(
    onClick: () -> Unit,
    isRunning: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Rotating wave animation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Pulse animation for outer aura
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val primaryColor = if (isRunning) ZenithAccent else ZenithCyan

    Box(
        modifier = modifier
            .size(220.dp)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Outer Particle & Wave Aura Canvas
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotationAngle }
        ) {
            val centerPt = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.width / 2f - 10.dp.toPx()

            val wavePoints = 48
            val path = Path()
            for (i in 0..wavePoints) {
                val angle = (i.toFloat() / wavePoints) * 2 * Math.PI.toFloat()
                val waveOffset = kotlin.math.sin(angle * 5 + (rotationAngle * Math.PI.toFloat() / 180f)) * 4.dp.toPx()
                val r = outerRadius + waveOffset
                val x = centerPt.x + r * kotlin.math.cos(angle)
                val y = centerPt.y + r * kotlin.math.sin(angle)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()

            drawPath(
                path = path,
                color = primaryColor.copy(alpha = 0.5f),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            )

            // Particles around perimeter
            for (p in 0..15) {
                val pAngle = (p.toFloat() / 16) * 2 * Math.PI.toFloat() + (rotationAngle * Math.PI.toFloat() / 180f)
                val pr = outerRadius + kotlin.math.sin(p * 2f) * 6.dp.toPx()
                val px = centerPt.x + pr * kotlin.math.cos(pAngle)
                val py = centerPt.y + pr * kotlin.math.sin(pAngle)
                drawCircle(
                    color = primaryColor.copy(alpha = 0.65f),
                    radius = (1.5 + (p % 3)).dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }

        // Ambient radial background glow
        Box(
            modifier = Modifier
                .size(180.dp * pulse)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.35f),
                            primaryColor.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // 3D Glass Orb Sphere Core
        Box(
            modifier = Modifier
                .size(160.dp)
                .shadow(
                    elevation = 28.dp,
                    shape = CircleShape,
                    spotColor = primaryColor
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor,
                            primaryColor.copy(alpha = 0.85f),
                            Color(0xFF003818),
                            Color(0xFF011409)
                        ),
                        center = Offset(160.dp.value * 0.35f, 160.dp.value * 0.25f),
                        radius = 220f
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor,
                            primaryColor.copy(alpha = 0.3f),
                            primaryColor.copy(alpha = 0.7f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Top Gloss Highlight Arc
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .height(52.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 6.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.38f),
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Center Power Icon with Glow Shadow
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Power Action",
                    tint = primaryColor.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(58.dp)
                        .graphicsLayer { alpha = 0.8f }
                )
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Power Action",
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }
        }
    }
}

@Composable
fun EqualizerWaveBar(
    isRunning: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    Row(
        modifier = modifier.height(16.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barCount = 13
        for (i in 0 until barCount) {
            val animDuration = 500 + (i % 5) * 120
            val hFactor by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(animDuration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            val heightDp = if (isRunning) (4 + (hFactor * (12 - kotlin.math.abs(i - 6)))).dp else 3.dp

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(heightDp)
                    .background(
                        color = if (isRunning) color else Color.Gray.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
fun PermissionBanner(onRequestPermission: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onRequestPermission),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0A12)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFFFF5252).copy(alpha = 0.18f), CircleShape)
                    .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.overlay_permission),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.tap_to_grant),
                    color = Color(0xFFFF8A8A),
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ImpactCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    barIndex: Int = 0,
    isRunning: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1324)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1C2D4A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.16f), CircleShape)
                        .border(1.dp, color.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            
            // Animated Wave Sparkline Graph
            AnimatedWaveSparklineGraph(
                color = color,
                isRunning = isRunning,
                cardIndex = barIndex
            )
        }
    }
}

@Composable
fun AnimatedWaveSparklineGraph(
    color: Color,
    isRunning: Boolean,
    cardIndex: Int = 0
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Smooth wave flow animation
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (isRunning) 2200 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Dynamic wave amplitude multiplier when active
    val ampMult by infiniteTransition.animateFloat(
        initialValue = if (isRunning) 0.85f else 0.98f,
        targetValue = if (isRunning) 1.25f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        val width = size.width
        val height = size.height
        if (width <= 0 || height <= 0) return@Canvas

        val path = Path()
        val fillPath = Path()

        val points = 32
        val baseFreq = if (cardIndex == 0) 2.2f else 3.0f

        fillPath.moveTo(0f, height)

        for (i in 0..points) {
            val normX = i.toFloat() / points
            val x = normX * width

            // Trend curve rising towards the right
            val trendY = height * (0.75f - normX * 0.45f)

            // Combined sine harmonics for organic wave look
            val wave1 = kotlin.math.sin((normX * baseFreq * 2 * Math.PI + phase).toDouble()).toFloat()
            val wave2 = kotlin.math.cos((normX * baseFreq * 1.4 * Math.PI - phase * 0.8).toDouble()).toFloat()

            val amplitude = (if (isRunning) 5.5.dp.toPx() else 3.0.dp.toPx()) * ampMult
            val y = (trendY + (wave1 + wave2 * 0.45f) * amplitude).coerceIn(3.dp.toPx(), height - 2.dp.toPx())

            if (i == 0) {
                path.moveTo(x, y)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        // Draw smooth gradient fill under the wave line
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    color.copy(alpha = if (isRunning) 0.50f else 0.25f),
                    color.copy(alpha = 0.02f)
                ),
                startY = 0f,
                endY = height
            )
        )

        // Draw glowing wave stroke line
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = if (isRunning) 2.2.dp.toPx() else 1.6.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}


@Composable
fun GamificationSection(totalTimeSaved: Long) {
    val totalHours = (totalTimeSaved / 3600000).toFloat()
    
    val (level, levelTitle, currentLevelThreshold, nextLevelThreshold) = when {
        totalHours < 1 -> Quadruple(0, "ECO NOVICE", 0f, 1f)
        totalHours < 5 -> Quadruple(1, "ENERGY SAVER", 1f, 5f)
        totalHours < 10 -> Quadruple(2, "BATTERY GUARDIAN", 5f, 10f)
        totalHours < 25 -> Quadruple(3, "SOLAR PROTECTOR", 10f, 25f)
        totalHours < 50 -> Quadruple(4, "GREEN WARRIOR", 25f, 50f)
        totalHours < 100 -> Quadruple(5, "OLED MASTER", 50f, 100f)
        else -> Quadruple(6, "ZENITH LEGEND", 100f, 200f)
    }
    
    val progress = ((totalHours - currentLevelThreshold) / (nextLevelThreshold - currentLevelThreshold)).coerceIn(0f, 1f)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1324)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1C2D4A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFFFD700).copy(alpha = 0.3f), Color(0xFFFFD700).copy(alpha = 0.05f))
                                ),
                                CircleShape
                            )
                            .border(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophy",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Level $level",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF00E676).copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFF00E676).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = levelTitle,
                                    color = Color(0xFF00E676),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "${String.format("%.1f", totalHours)} Hours Saved Total",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Glowing XP Progress Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress to Level ${level + 1}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = Color(0xFF00E676),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = progress)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF00E5FF), Color(0xFF00E676))
                                )
                            )
                    )
                }
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

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
    
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                expanded = isExpanded
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1324)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1C2D4A))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
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
                        .size(46.dp)
                        .background(iconColor.copy(alpha = 0.16f), CircleShape)
                        .border(1.dp, iconColor.copy(alpha = 0.35f), CircleShape),
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
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
                        .border(1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ChevronRight,
                    contentDescription = if (expanded) "Collapse section" else "Expand section",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    HorizontalDivider(color = Color(0xFF1C2D4A), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    content()
                }
            }
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
                checkedTrackColor = Color(0xFF00E676),
                uncheckedThumbColor = Color(0xFF64748B),
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

@Composable
fun SmartTriggerCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    bottomContent: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0C1322)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B2C46))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(iconTint.copy(alpha = 0.18f), CircleShape)
                            .border(1.dp, iconTint.copy(alpha = 0.40f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF00E676),
                        uncheckedThumbColor = Color(0xFF64748B),
                        uncheckedTrackColor = Color(0xFF1E293B),
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            bottomContent()
        }
    }
}

@Composable
fun PocketModeWaveGraphic() {
    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        val width = size.width
        val height = size.height
        val path = Path()
        val points = 40
        for (i in 0..points) {
            val normX = i.toFloat() / points
            val x = normX * width
            val wave = kotlin.math.sin((normX * 3.5 * Math.PI + phase).toDouble()).toFloat()
            val y = height / 2 + wave * 9.dp.toPx()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = Color(0xFF00E676),
            style = Stroke(width = 2.2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}


@Composable
fun ShakeToWakeGraphic() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(14.dp, 22.dp)) {
            val path = Path().apply {
                moveTo(12.dp.toPx(), 2.dp.toPx())
                cubicTo(2.dp.toPx(), 6.dp.toPx(), 2.dp.toPx(), 16.dp.toPx(), 12.dp.toPx(), 20.dp.toPx())
            }
            drawPath(path, color = Color(0xFF00E5FF), style = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .width(22.dp)
                .height(28.dp)
                .background(Color(0xFF00E5FF).copy(alpha = 0.2f), RoundedCornerShape(5.dp))
                .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(5.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(Color(0xFF00E5FF), CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        androidx.compose.foundation.Canvas(modifier = Modifier.size(14.dp, 22.dp)) {
            val path = Path().apply {
                moveTo(2.dp.toPx(), 2.dp.toPx())
                cubicTo(12.dp.toPx(), 6.dp.toPx(), 12.dp.toPx(), 16.dp.toPx(), 2.dp.toPx(), 20.dp.toPx())
            }
            drawPath(path, color = Color(0xFF00E5FF), style = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }
    }
}

@Composable
fun FloatingButtonGraphic() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
        ) {
            val width = size.width
            val height = size.height
            val path = Path()
            val points = 32
            for (i in 0..points) {
                val normX = i.toFloat() / points
                val x = normX * width
                val wave = kotlin.math.sin((normX * 3.2 * Math.PI).toDouble()).toFloat()
                val y = height / 2 + wave * 4.dp.toPx()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = Color(0xFFFFB300),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color(0xFFFFB300).copy(alpha = 0.15f), CircleShape)
                .border(1.5.dp, Color(0xFFFFB300), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Color(0xFF00E676), CircleShape)
                    .border(1.dp, Color.White, CircleShape)
            )
        }
    }
}

@Composable
fun LanguageCard() {
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

    val currentCode = context.getSharedPreferences("BlackScreenStats", Context.MODE_PRIVATE)
        .getString("app_language", "en") ?: "en"
    val currentName = languages.find { it.first == currentCode }?.second ?: "English"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1322)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B2C46))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFF00E5FF).copy(alpha = 0.18f), CircleShape)
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.40f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language settings icon",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.language),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.select_language),
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { expanded = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentName,
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Open language menu",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color(0xFF0F172A)).heightIn(max = 300.dp)
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

            Spacer(modifier = Modifier.height(10.dp))

            LanguageGraphic()
        }
    }
}

@Composable
fun LanguageGraphic() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val sampleTags = listOf("English", "Somali", "العربية", "Español", "Deutsch")
        sampleTags.forEachIndexed { index, tag ->
            Box(
                modifier = Modifier
                    .background(
                        if (index == 0) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1E293B),
                        CircleShape
                    )
                    .border(
                        1.dp,
                        if (index == 0) Color(0xFF00E5FF).copy(alpha = 0.5f) else Color(0xFF334155),
                        CircleShape
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = tag,
                    color = if (index == 0) Color(0xFF00E5FF) else Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AodGraphic() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(Color(0xFF050810), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF131F33), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFF00E676), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "10:09 AM",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mon, Oct 24",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = "Battery charging icon",
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "88%",
                    color = Color(0xFF00E676),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OledPixelShiftGraphic() {
    val infiniteTransition = rememberInfiniteTransition()
    val shiftX by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        val width = size.width
        val height = size.height
        
        val rows = 3
        val cols = 14
        val spacingX = width / (cols + 1)
        val spacingY = height / (rows + 1)

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cx = (c + 1) * spacingX + (if (r % 2 == 0) shiftX else -shiftX)
                val cy = (r + 1) * spacingY
                
                val pixelColor = when ((r + c) % 3) {
                    0 -> Color(0xFFFFB300)
                    1 -> Color(0xFF00E676)
                    else -> Color(0xFF00E5FF)
                }
                
                drawCircle(
                    color = pixelColor.copy(alpha = 0.75f),
                    radius = 2.5.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(cx, cy)
                )
            }
        }
    }
}

@Composable
fun SkipUnlockGraphic() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.TouchApp,
            contentDescription = "Tap gesture icon",
            tint = Color(0xFFAB47BC),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .width(40.dp)
                .height(16.dp)
        ) {
            val width = size.width
            val height = size.height
            val path = Path().apply {
                moveTo(0f, height / 2)
                lineTo(width - 6.dp.toPx(), height / 2)
            }
            drawPath(
                path = path,
                color = Color(0xFFAB47BC),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )
            )
            val arrowHead = Path().apply {
                moveTo(width - 8.dp.toPx(), height / 2 - 4.dp.toPx())
                lineTo(width, height / 2)
                lineTo(width - 8.dp.toPx(), height / 2 + 4.dp.toPx())
            }
            drawPath(
                path = arrowHead,
                color = Color(0xFFAB47BC),
                style = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .size(26.dp)
                .background(Color(0xFF00E676).copy(alpha = 0.2f), CircleShape)
                .border(1.5.dp, Color(0xFF00E676), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Unlocked success icon",
                tint = Color(0xFF00E676),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
