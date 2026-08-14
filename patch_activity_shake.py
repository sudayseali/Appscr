with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'r') as f:
    content = f.read()

target_failure = """                        com.noxscreen.app.security.AuthenticationManager.startAuthentication(
                            onSuccess = { finish() },
                            onFailure = { /* do nothing, stay in BlackoutActivity */ }
                        )"""

replacement_failure = """                        com.noxscreen.app.security.AuthenticationManager.startAuthentication(
                            onSuccess = { finish() },
                            onFailure = { 
                                val errorIntent = Intent("SHOW_BIOMETRIC_ERROR")
                                sendBroadcast(errorIntent)
                            }
                        )"""

target_compose_state = """    var isUnlockScreenVisible by remember { mutableStateOf(initialShowUnlock) }"""

replacement_compose_state = """    var isUnlockScreenVisible by remember { mutableStateOf(initialShowUnlock) }
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
    }"""

target_compose_view = """        if (isUnlockScreenVisible) {
            UnlockScreenView(onUnlock = onUnlock)
        }
    }"""

replacement_compose_view = """        if (isUnlockScreenVisible) {
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
    }"""

target_import = """import androidx.compose.material.icons.filled.BatteryFull"""

replacement_import = """import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Lock"""

if target_failure in content and target_compose_state in content and target_compose_view in content and target_import in content:
    content = content.replace(target_failure, replacement_failure)
    content = content.replace(target_compose_state, replacement_compose_state)
    content = content.replace(target_compose_view, replacement_compose_view)
    content = content.replace(target_import, replacement_import)
    with open('app/src/main/java/com/noxscreen/app/BlackoutActivity.kt', 'w') as f:
        f.write(content)
    print("Patched BlackoutActivity")
else:
    print("Targets not found")
