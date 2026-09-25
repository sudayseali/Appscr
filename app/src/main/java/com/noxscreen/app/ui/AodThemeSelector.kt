package com.noxscreen.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
        Pair("green", Color(0xFF69F0AE)),
        Pair("blue", Color(0xFF82B1FF)),
        Pair("yellow", Color(0xFFFFD54F)),
        Pair("pink", Color(0xFFFF80AB))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "AOD Theme Color",
            color = ZenithTextMuted,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            themes.forEach { (id, color) ->
                val isSelected = selectedTheme == id
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            2.dp,
                            if (isSelected) ZenithAccent else Color.Transparent,
                            CircleShape
                        )
                        .clickable { onThemeSelected(id) }
                )
            }
        }
    }
}
