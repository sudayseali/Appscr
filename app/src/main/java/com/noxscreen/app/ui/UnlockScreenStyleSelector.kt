package com.noxscreen.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxscreen.app.ui.theme.*

@Composable
fun UnlockScreenStyleSelector(
    selectedStyle: String,
    onStyleSelected: (String) -> Unit
) {
    val styles = listOf(
        Pair("button", "Button"),
        Pair("swipe", "Swipe Up"),
        Pair("icon", "Lock Icon")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "Screen Unlock Style",
            color = ZenithTextMuted,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            styles.forEach { (id, name) ->
                val isSelected = selectedStyle == id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) ZenithAccent.copy(alpha = 0.2f) else ZenithCardBorder)
                        .border(
                            1.dp,
                            if (isSelected) ZenithAccent else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onStyleSelected(id) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        color = if (isSelected) ZenithAccent else Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
