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
                .width(320.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF151929)) // Darkest blue/gray background
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                
                // Top Illustration Placeholder
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(ZenithAccent.copy(alpha = 0.1f), Color.Transparent)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color(0xFF1E243A), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFF2A314A), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(6.dp).background(Color(0xFFEF4444), CircleShape))
                                    Box(modifier = Modifier.size(6.dp).background(Color(0xFFF59E0B), CircleShape))
                                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF10B981), CircleShape))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(60.dp, 40.dp)
                                    .background(Color(0xFF4C3B73), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("AD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.size(40.dp, 4.dp).background(Color(0xFF2A314A), RoundedCornerShape(2.dp)))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Loading Ad",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Please wait a moment while we load\nan ad for you",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color(0xFF1E243A), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress / 100f)
                            .background(ZenithAccent, RoundedCornerShape(4.dp))
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${progress.toInt()}%",
                    color = ZenithAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Features Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FeatureItem(Icons.Default.Security, "Ad is secure")
                    FeatureItem(Icons.Default.Bolt, "Quick & safe", ZenithAccent) // Using Accent for middle icon
                    FeatureItem(Icons.Default.Block, "No personal data", Color(0xFFF59E0B)) // Using amber for block icon
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Cancel Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFEF4444).copy(alpha = 0.1f), CircleShape)
                            .border(1.dp, Color(0xFFEF4444), CircleShape)
                            .clickable { onDismissRequest() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color(0xFFEF4444))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Cancel", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, tint: Color = ZenithAccent) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
