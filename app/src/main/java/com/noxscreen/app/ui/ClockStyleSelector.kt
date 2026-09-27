package com.noxscreen.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxscreen.app.ui.theme.ZenithCardBorder
import com.noxscreen.app.ui.theme.ZenithTextMuted
import kotlin.math.cos
import kotlin.math.sin

private data class AodStyleOption(
    val id: String,
    val title: String,
    val subtitle: String
)

private fun resolveThemePreviewColors(themeColor: String): Pair<Color, Color> {
    return when (themeColor.lowercase()) {
        "green" -> Color(0xFF00FF88) to Color(0xFF00BFA5)
        "yellow" -> Color(0xFFFFE57F) to Color(0xFFFF8F00)
        "pink" -> Color(0xFFFF80AB) to Color(0xFFF50057)
        "purple" -> Color(0xFFB388FF) to Color(0xFF651FFF)
        "blue" -> Color(0xFF82B1FF) to Color(0xFF2962FF)
        else -> Color(0xFF00E5FF) to Color(0xFF006BFF) // "white" / signature cyan-blue
    }
}

@Composable
fun ClockStyleSelector(
    selectedStyle: String,
    onStyleSelected: (String) -> Unit,
    selectedTheme: String = "white"
) {
    val styles = listOf(
        AodStyleOption("default", "Orbital Neon", "Signature Ring"),
        AodStyleOption("huge", "Neon Outline", "Hollow Cyber"),
        AodStyleOption("analog", "Orbital Chrono", "Neon Analog"),
        AodStyleOption("dino", "Neon Pulse", "Dual Portal")
    )

    val (neonPrimary, neonSecondary) = resolveThemePreviewColors(selectedTheme)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AOD Clock Style",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = styles.firstOrNull { it.id == selectedStyle }?.title ?: "Orbital Neon",
                color = neonPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            styles.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { option ->
                        val isSelected = selectedStyle == option.id
                        OrbitalStyleCard(
                            option = option,
                            isSelected = isSelected,
                            neonPrimary = neonPrimary,
                            neonSecondary = neonSecondary,
                            onClick = { onStyleSelected(option.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrbitalStyleCard(
    option: AodStyleOption,
    isSelected: Boolean,
    neonPrimary: Color,
    neonSecondary: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) neonPrimary else ZenithCardBorder,
        animationSpec = tween(220),
        label = "style_border"
    )
    val cardBg by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF071326) else Color(0xFF060B16),
        animationSpec = tween(220),
        label = "style_bg"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                brush = if (isSelected) {
                    Brush.linearGradient(listOf(neonPrimary, neonSecondary))
                } else {
                    Brush.linearGradient(listOf(borderColor, borderColor))
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        // Miniature Black Screen AOD Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black)
                .border(
                    width = 0.8.dp,
                    color = if (isSelected) neonPrimary.copy(alpha = 0.35f) else Color(0xFF111C2E),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height * 0.44f
                val r = size.height * 0.34f

                // Ambient glow inside orbital ring
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            neonPrimary.copy(alpha = 0.18f),
                            neonSecondary.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = r * 1.25f
                    ),
                    radius = r * 1.25f,
                    center = Offset(cx, cy)
                )

                val arcTopLeft = Offset(cx - r, cy - r)
                val arcSize = Size(r * 2f, r * 2f)

                // Top glowing crescent arc
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(neonSecondary, neonPrimary, Color.White),
                        start = Offset(cx - r, cy - r),
                        end = Offset(cx + r, cy)
                    ),
                    startAngle = 208f,
                    sweepAngle = 124f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Bottom-left crescent arc
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(neonSecondary, neonPrimary.copy(alpha = 0.85f)),
                        start = Offset(cx - r, cy),
                        end = Offset(cx, cy + r)
                    ),
                    startAngle = 76f,
                    sweepAngle = 76f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
                )

                // Bottom-right delicate arc
                drawArc(
                    color = neonPrimary.copy(alpha = 0.65f),
                    startAngle = 26f,
                    sweepAngle = 38f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = 1.1.dp.toPx(), cap = StrokeCap.Round)
                )

                if (option.id == "dino") {
                    val innerR = r * 0.82f
                    drawArc(
                        color = neonPrimary.copy(alpha = 0.55f),
                        startAngle = 195f,
                        sweepAngle = 150f,
                        useCenter = false,
                        topLeft = Offset(cx - innerR, cy - innerR),
                        size = Size(innerR * 2f, innerR * 2f),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f)
                        )
                    )
                }

                if (option.id == "analog") {
                    val dialR = r * 0.78f
                    for (i in 0 until 12) {
                        val deg = i * 30f - 90f
                        val rad = Math.toRadians(deg.toDouble())
                        val outer = dialR
                        val inner = if (i % 3 == 0) dialR - 3.5.dp.toPx() else dialR - 1.8.dp.toPx()
                        drawLine(
                            color = if (i % 3 == 0) Color.White else neonPrimary.copy(alpha = 0.6f),
                            start = Offset(
                                cx + outer * cos(rad).toFloat(),
                                cy + outer * sin(rad).toFloat()
                            ),
                            end = Offset(
                                cx + inner * cos(rad).toFloat(),
                                cy + inner * sin(rad).toFloat()
                            ),
                            strokeWidth = if (i % 3 == 0) 1.4.dp.toPx() else 0.8.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                    // Hour hand (pointing to ~10)
                    val hRad = Math.toRadians(-145.0)
                    drawLine(
                        color = Color.White,
                        start = Offset(cx, cy),
                        end = Offset(
                            cx + dialR * 0.48f * cos(hRad).toFloat(),
                            cy + dialR * 0.48f * sin(hRad).toFloat()
                        ),
                        strokeWidth = 2.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Minute hand (pointing to ~2)
                    val mRad = Math.toRadians(-35.0)
                    drawLine(
                        color = neonPrimary,
                        start = Offset(cx, cy),
                        end = Offset(
                            cx + dialR * 0.72f * cos(mRad).toFloat(),
                            cy + dialR * 0.72f * sin(mRad).toFloat()
                        ),
                        strokeWidth = 1.7.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = neonPrimary,
                        radius = 2.2.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }

                // Mini glowing date pill at bottom of orbital ring
                val pillW = 46.dp.toPx()
                val pillH = 11.dp.toPx()
                val pillTop = cy + r * 0.62f
                val pillLeft = cx - pillW / 2f
                drawRoundRect(
                    color = Color(0xFF030812),
                    topLeft = Offset(pillLeft, pillTop),
                    size = Size(pillW, pillH),
                    cornerRadius = CornerRadius(pillH / 2f, pillH / 2f)
                )
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(neonSecondary, neonPrimary)),
                    topLeft = Offset(pillLeft, pillTop),
                    size = Size(pillW, pillH),
                    cornerRadius = CornerRadius(pillH / 2f, pillH / 2f),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Mini green battery indicator dot below pill
                val batY = pillTop + pillH + 5.5.dp.toPx()
                drawRoundRect(
                    color = Color(0xFF00E676),
                    topLeft = Offset(cx - 7.dp.toPx(), batY - 2.5.dp.toPx()),
                    size = Size(3.5.dp.toPx(), 5.dp.toPx()),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.85f),
                    start = Offset(cx - 1.dp.toPx(), batY),
                    end = Offset(cx + 7.dp.toPx(), batY),
                    strokeWidth = 1.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Digital Time Overlay for digital styles ("default", "huge", "dino")
            if (option.id != "analog") {
                Box(
                    modifier = Modifier
                        .offset(y = (-6).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "12",
                            style = TextStyle(
                                color = if (option.id == "huge") Color.White.copy(alpha = 0.92f) else Color.White,
                                fontSize = if (option.id == "huge") 21.sp else 20.sp,
                                fontWeight = if (option.id == "huge") FontWeight.ExtraBold else FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Text(
                            text = ":",
                            style = TextStyle(
                                color = neonPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 1.5.dp)
                        )
                        Text(
                            text = "23",
                            style = TextStyle(
                                brush = Brush.verticalGradient(
                                    colors = listOf(neonPrimary, neonSecondary)
                                ),
                                fontSize = if (option.id == "huge") 21.sp else 20.sp,
                                fontWeight = if (option.id == "huge") FontWeight.ExtraBold else FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }

                    if (option.id == "dino") {
                        // Horizontal laser horizon line across the digits
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .width(54.dp)
                                .height(1.2.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            neonPrimary,
                                            Color.White,
                                            neonPrimary,
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.88f),
                    fontSize = 12.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = option.subtitle,
                    color = if (isSelected) neonPrimary else ZenithTextMuted,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) neonPrimary else ZenithCardBorder)
            )
        }
    }
}
