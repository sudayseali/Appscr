package com.noxscreen.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxscreen.app.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BlackoutActivity : ComponentActivity() {
    private val authLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            finish()
        } else {
            val errorIntent = android.content.Intent("SHOW_BIOMETRIC_ERROR").apply {
                setPackage(packageName)
            }
            sendBroadcast(errorIntent)
        }
    }

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val homeIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                    addCategory(android.content.Intent.CATEGORY_HOME)
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(homeIntent)
            }
        })
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        // Set brightness to minimum
        val layoutParams = window.attributes
        layoutParams.screenBrightness = 0f
        layoutParams.buttonBrightness = 0f
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            layoutParams.preferredRefreshRate = 30f
        }
        window.attributes = layoutParams
        
        val showUnlockPageImmediately = intent.getBooleanExtra("showUnlockPageImmediately", false)
        
        setContent {
            MyApplicationTheme(darkTheme = true) {
                BlackoutScreen(initialShowUnlock = showUnlockPageImmediately, onUnlock = { 
                    val settings = com.noxscreen.app.automation.AutomationSettings(this)
                    val isBiometricEnabled = settings.getConfig().isBiometricEnabled
                    if (isBiometricEnabled) {
                        com.noxscreen.app.security.AuthenticationManager.setAuthenticating()
                        val intent = android.content.Intent(this@BlackoutActivity, BiometricAuthActivity::class.java)
                        authLauncher.launch(intent)
                    } else {
                        finish() 
                    }
                })
            }
        }
    }
}

@Composable
fun BlackoutScreen(initialShowUnlock: Boolean = false, onUnlock: () -> Unit) {
    val context = LocalContext.current
    val automationSettings = remember { com.noxscreen.app.automation.AutomationSettings(context) }
    val autoConfig = remember { automationSettings.getConfig() }
    
    var tapCount by remember { mutableStateOf(0) }
    var isUnlockScreenVisible by remember { mutableStateOf(initialShowUnlock) }
    var showError by remember { mutableStateOf(false) }
    
    DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(c: android.content.Context?, intent: Intent?) {
                if (intent?.action == "SHOW_BIOMETRIC_ERROR") {
                    showError = true
                    isUnlockScreenVisible = false
                }
            }
        }
        val filter = android.content.IntentFilter("SHOW_BIOMETRIC_ERROR")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {}
        }
    }
    
    LaunchedEffect(showError) {
        if (showError) {
            delay(1000)
            showError = false
        }
    }
    
    LaunchedEffect(tapCount) {
        if (tapCount > 0 && !isUnlockScreenVisible) {
            delay(1500)
            tapCount = 0
        }
    }
    
    LaunchedEffect(isUnlockScreenVisible) {
        val window = (context as? android.app.Activity)?.window
        if (isUnlockScreenVisible) {
            window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            delay(10000)
            isUnlockScreenVisible = false
        } else {
            delay(5000)
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    
    val fadeAlpha = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(Unit) {
        fadeAlpha.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = 2000,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = fadeAlpha.value))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!isUnlockScreenVisible) {
                    tapCount++
                    if (tapCount >= autoConfig.tapsToWake) {
                        if (autoConfig.isSkipUnlockScreenEnabled) {
                            onUnlock()
                        } else {
                            isUnlockScreenVisible = true
                        }
                        tapCount = 0
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (isUnlockScreenVisible) {
            UnlockScreenView(onUnlock = onUnlock)
        }
        
        if (showError) {
            val offsetX = remember { androidx.compose.animation.core.Animatable(0f) }
            LaunchedEffect(Unit) {
                for (i in 0..5) {
                    offsetX.animateTo(20f, animationSpec = androidx.compose.animation.core.tween(50))
                    offsetX.animateTo(-20f, animationSpec = androidx.compose.animation.core.tween(50))
                }
                offsetX.animateTo(0f, animationSpec = androidx.compose.animation.core.tween(50))
            }
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Error",
                tint = Color.Red,
                modifier = Modifier
                    .size(64.dp)
                    .offset(x = offsetX.value.dp)
            )
        }
    }
}

@Composable
fun UnlockScreenView(onUnlock: () -> Unit) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    var currentDate by remember { mutableStateOf(SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())) }
    
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            currentDate = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 100.dp)
        ) {
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentDate,
                color = Color.White,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Icon(
                imageVector = Icons.Default.BatteryFull,
                contentDescription = "Battery",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp)
                .clickable { onUnlock() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "UNLOCK",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}
