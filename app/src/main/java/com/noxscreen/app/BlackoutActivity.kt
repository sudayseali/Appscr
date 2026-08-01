package com.noxscreen.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
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
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.noxscreen.app.BIOMETRIC_SUCCESS") {
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val filter = IntentFilter("com.noxscreen.app.BIOMETRIC_SUCCESS")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
        
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
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
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            MyApplicationTheme(darkTheme = true) {
                BlackoutScreen(onUnlock = { 
                    val settings = com.noxscreen.app.automation.AutomationSettings(this)
                    val isBiometricEnabled = settings.getConfig().isBiometricEnabled
                    if (isBiometricEnabled) {
                        val intent = Intent(this, BiometricAuthActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
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
        if (isUnlockScreenVisible) {
            delay(10000)
            isUnlockScreenVisible = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!isUnlockScreenVisible) {
                    tapCount++
                    if (tapCount >= autoConfig.tapsToWake) {
                        isUnlockScreenVisible = true
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
        // Top section with time, date, and battery
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
        
        // Bottom unlock section
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
