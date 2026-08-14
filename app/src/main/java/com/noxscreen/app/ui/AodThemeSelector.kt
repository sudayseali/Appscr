package com.noxscreen.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxscreen.app.ui.theme.*

@Composable
fun AodThemeSelector(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val themes = listOf(
        Pair("white", Color.White),
        Pair("green", Color(0xFF00E676)),
        Pair("blue", Color(0xFF00E5FF)),
        Pair("yellow", Color(0xFFFFD54F)),
        Pair("pink", Color(0xFFFF4081))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "Accent Luminescence",
            color = ZenithTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            themes.forEach { (id, color) ->
                val isSelected = selectedTheme == id
                val haloSize by animateDpAsState(
                    targetValue = if (isSelected) 44.dp else 38.dp,
                    animationSpec = tween(200),
                    label = "haloSize"
                )

                Box(
                    modifier = Modifier
                        .size(haloSize)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) color.copy(alpha = 0.22f) else Color.Transparent
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) color else Color(0xFF1E2F4D),
                            shape = CircleShape
                        )
                        .clickable { onThemeSelected(id) },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF020612),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

