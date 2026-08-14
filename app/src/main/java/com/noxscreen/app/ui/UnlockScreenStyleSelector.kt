package com.noxscreen.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.SwipeUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxscreen.app.ui.theme.*

data class UnlockOption(
    val id: String,
    val name: String,
    val icon: ImageVector
)

@Composable
fun UnlockScreenStyleSelector(
    selectedStyle: String,
    onStyleSelected: (String) -> Unit
) {
    val styles = listOf(
        UnlockOption("button", "Button", Icons.Default.SmartButton),
        UnlockOption("swipe", "Swipe Up", Icons.Default.SwipeUp),
        UnlockOption("icon", "Lock Icon", Icons.Default.Lock)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "Wake / Unlock Gesture Style",
            color = ZenithTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            styles.forEach { option ->
                val isSelected = selectedStyle == option.id
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFAB47BC).copy(alpha = 0.16f) else Color(0xFF0F172A),
                    animationSpec = tween(250),
                    label = "unlockBg"
                )
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFAB47BC) else Color(0xFF1E2F4D),
                    animationSpec = tween(250),
                    label = "unlockBorder"
                )
                val tintColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFE1BEE7) else ZenithTextMuted,
                    animationSpec = tween(250),
                    label = "unlockTint"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(
                            if (isSelected) 1.5.dp else 1.dp,
                            borderColor,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onStyleSelected(option.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = option.name,
                            tint = tintColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = option.name,
                            color = tintColor,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

