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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
        
        setContent {
            MyApplicationTheme(darkTheme = true) {
                BlackoutScreen(onUnlock = { 
                    val settings = com.noxscreen.app.automation.AutomationSettings(this)
                    val isBiometricEnabled = settings.getConfig().isBiometricEnabled
                    if (isBiometricEnabled) {
                        com.noxscreen.app.security.AuthenticationManager.startAuthentication(
                            onSuccess = { finish() },
                            onFailure = { /* do nothing, stay in BlackoutActivity */ }
                        )
                        val intent = Intent(this, BiometricAuthActivity::class.java)
                        startActivity(intent)
                    } else {
                        finish() 
                    }
                })
            }
        }
    }
}

@Composable
fun BlackoutScreen(onUnlock: () -> Unit) {
    val context = LocalContext.current
    val automationSettings = remember { com.noxscreen.app.automation.AutomationSettings(context) }
    val autoConfig = remember { automationSettings.getConfig() }
    
    var tapCount by remember { mutableStateOf(0) }
    var isUnlockScreenVisible by remember { mutableStateOf(false) }
    
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
