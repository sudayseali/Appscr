package com.noxscreen.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxscreen.app.ui.theme.*

data class ClockStyleOption(
    val id: String,
    val name: String,
    val icon: ImageVector
)

@Composable
fun ClockStyleSelector(
    selectedStyle: String,
    onStyleSelected: (String) -> Unit
) {
    val styles = listOf(
        ClockStyleOption("default", "Minimal", Icons.Default.AccessTime),
        ClockStyleOption("huge", "Huge", Icons.Default.Numbers),
        ClockStyleOption("analog", "Analog", Icons.Default.Schedule),
        ClockStyleOption("dino", "Dino", Icons.Default.Pets)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "Clock Display Style",
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
                    targetValue = if (isSelected) ZenithAccent.copy(alpha = 0.16f) else Color(0xFF0F172A),
                    animationSpec = tween(250),
                    label = "clockBg"
                )
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) ZenithAccent else Color(0xFF1E2F4D),
                    animationSpec = tween(250),
                    label = "clockBorder"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) ZenithAccent else ZenithTextMuted,
                    animationSpec = tween(250),
                    label = "clockText"
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
                            tint = textColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = option.name,
                            color = textColor,
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

