package com.noxscreen.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxscreen.app.ui.theme.ZenithCardBorder
import com.noxscreen.app.ui.theme.ZenithTextMuted

private data class AodNeonThemeOption(
    val id: String,
    val label: String,
    val topColor: Color,
    val bottomColor: Color
)

@Composable
fun AodThemeSelector(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val themes = listOf(
        AodNeonThemeOption("white", "Cyber Cyan", Color(0xFF00E5FF), Color(0xFF006BFF)),
        AodNeonThemeOption("green", "Emerald", Color(0xFF00FF88), Color(0xFF00BFA5)),
        AodNeonThemeOption("blue", "Electric", Color(0xFF82B1FF), Color(0xFF2962FF)),
        AodNeonThemeOption("yellow", "Solar Gold", Color(0xFFFFE57F), Color(0xFFFF8F00)),
        AodNeonThemeOption("pink", "Neon Rose", Color(0xFFFF80AB), Color(0xFFF50057)),
        AodNeonThemeOption("purple", "Ultraviolet", Color(0xFFB388FF), Color(0xFF651FFF))
    )

    val activeOption = themes.firstOrNull { it.id == selectedTheme } ?: themes.first()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AOD Neon Theme",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = activeOption.label,
                color = activeOption.topColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            themes.forEach { option ->
                val isSelected = selectedTheme == option.id
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) option.topColor else ZenithCardBorder,
                    animationSpec = tween(200),
                    label = "theme_swatch_border"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF071326) else Color(0xFF060B16))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onThemeSelected(option.id) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val r = size.minDimension / 2f - 2.dp.toPx()
                            val center = Offset(size.width / 2f, size.height / 2f)

                            // Ambient halo
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        option.topColor.copy(alpha = if (isSelected) 0.35f else 0.16f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = r * 1.15f
                                ),
                                radius = r * 1.15f,
                                center = center
                            )

                            // Orbital crescent arc swatch
                            drawArc(
                                brush = Brush.linearGradient(
                                    colors = listOf(option.bottomColor, option.topColor, Color.White)
                                ),
                                startAngle = 200f,
                                sweepAngle = 140f,
                                useCenter = false,
                                topLeft = Offset(center.x - r, center.y - r),
                                size = Size(r * 2f, r * 2f),
                                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                            )

                            drawArc(
                                color = option.bottomColor.copy(alpha = 0.85f),
                                startAngle = 60f,
                                sweepAngle = 85f,
                                useCenter = false,
                                topLeft = Offset(center.x - r, center.y - r),
                                size = Size(r * 2f, r * 2f),
                                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Inner glowing core dot
                            drawCircle(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White, option.topColor, option.bottomColor)
                                ),
                                radius = r * 0.48f,
                                center = center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = option.label.split(" ").last(),
                        color = if (isSelected) Color.White else ZenithTextMuted,
                        fontSize = 9.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
