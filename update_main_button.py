import re

file_path = "app/src/main/java/com/noxscreen/app/MainActivity.kt"

with open(file_path, "r") as f:
    code = f.read()

# 1. Add missing imports if needed
imports_to_add = """import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke"""

if "import androidx.compose.ui.geometry.Offset" not in code:
    code = code.replace("import androidx.compose.ui.res.stringResource", "import androidx.compose.ui.res.stringResource\n" + imports_to_add)

# 2. Replace Central Power Core Box & Text in ZenithApp
old_central_section = """            // Central Power Core Button
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
            )"""

new_central_section = """            // Central Power Core Button & Floating Action Button Container
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
                            .background(
                                if (isFloatingOn) ZenithAccent.copy(alpha = 0.15f) else ZenithCardGlow
                            )
                            .border(
                                1.5.dp,
                                if (isFloatingOn) ZenithAccent else ZenithCardBorder,
                                CircleShape
                            )
                            .clickable {
                                autoConfig = autoConfig.copy(hideFloatingButton = !autoConfig.hideFloatingButton)
                                automationSettings.updateConfig(autoConfig)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_bolt),
                            contentDescription = "Floating Action Button",
                            tint = if (isFloatingOn) ZenithAccent else ZenithTextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Floating",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isFloatingOn) "ON" else "OFF",
                        color = if (isFloatingOn) ZenithAccent else ZenithTextMuted,
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
            )"""

if old_central_section in code:
    code = code.replace(old_central_section, new_central_section)
else:
    print("WARNING: old_central_section not found exact match")

# 3. Replace PowerPulseButton & add EqualizerWaveBar
old_power_button = """@Composable
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
}"""

new_power_button = """@Composable
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
}"""

if old_power_button in code:
    code = code.replace(old_power_button, new_power_button)
else:
    print("WARNING: old_power_button not found exact match")

with open(file_path, "w") as f:
    f.write(code)

print("Updated MainActivity.kt successfully!")
