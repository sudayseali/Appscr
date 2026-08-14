package com.noxscreen.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.noxscreen.app.ui.theme.*

@Composable
fun LoadingAdDialog(
    onDismissRequest: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }
    
    // Simulate loading progress
    LaunchedEffect(Unit) {
        val animation = TargetBasedAnimation(
            animationSpec = tween(3000, easing = LinearEasing),
            typeConverter = Float.VectorConverter,
            initialValue = 0f,
            targetValue = 100f
        )
        val startTime = withFrameNanos { it }
        do {
            val playTime = withFrameNanos { it } - startTime
            progress = animation.getValueFromNanos(playTime)
        } while (progress < 100f)
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(330.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF090E1A)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ZenithAccent.copy(alpha = 0.4f),
                            Color(0xFF1E2F4D)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Glowing Shield Orb
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(ZenithAccent.copy(alpha = 0.20f), Color.Transparent)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF1A2744), Color(0xFF0D1527))
                                )
                            )
                            .border(1.5.dp, ZenithAccent.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = ZenithAccent,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Text(
                    text = "Preparing Ad Experience",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "Thank you for supporting NoxScreen.\nYour style will unlock right after.",
                    color = ZenithTextMuted,
                    fontSize = 12.5.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Progress Bar with Gradient & Glowing Indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF16233B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = (progress / 100f).coerceIn(0f, 1f))
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(ZenithCyan, ZenithAccent)
                                )
                            )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Loading media stream...",
                        color = ZenithTextSubtle,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${progress.toInt()}%",
                        color = ZenithAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Features Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF070D18))
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FeatureItem(Icons.Default.Security, "Encrypted", ZenithAccent)
                    FeatureItem(Icons.Default.Bolt, "Fast-stream", ZenithCyan)
                    FeatureItem(Icons.Default.Block, "Zero tracking", ZenithAmber)
                }
                
                Spacer(modifier = Modifier.height(22.dp))
                
                // Dismiss Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onDismissRequest() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cancel",
                        color = Color(0xFFEF5350),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, tint: Color = ZenithAccent) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text, color = Color.White.copy(alpha = 0.85f), fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
    }
}

