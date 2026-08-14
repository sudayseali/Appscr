package com.noxscreen.app.ui

import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import com.noxscreen.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppItem(
    val packageName: String,
    val appName: String,
    val iconBitmap: ImageBitmap?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionDialog(
    initialSelectedApps: Set<String>,
    onDismissRequest: () -> Unit,
    onAppsSelected: (Set<String>) -> Unit
) {
    val context = LocalContext.current
    var installedApps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    val selectedApps = remember { mutableStateListOf<String>().apply { addAll(initialSelectedApps) } }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val apps = packages
                .filter { pm.getLaunchIntentForPackage(it.packageName) != null && it.packageName != context.packageName }
                .map { appInfo ->
                    val appName = pm.getApplicationLabel(appInfo).toString()
                    val iconBitmap = try {
                        val drawable = pm.getApplicationIcon(appInfo)
                        drawable.toBitmap(width = 96, height = 96).asImageBitmap()
                    } catch (e: Exception) {
                        null
                    }
                    AppItem(
                        packageName = appInfo.packageName,
                        appName = appName,
                        iconBitmap = iconBitmap
                    )
                }
                .sortedBy { it.appName.lowercase() }

            withContext(Dispatchers.Main) {
                installedApps = apps
                isLoading = false
            }
        }
    }

    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = Color(0xFF0B1220),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Top handle indicator
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(4.dp)
                        .background(Color(0xFF334155), CircleShape)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Select Apps to Block",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Search Row
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    placeholder = { Text("Search installed applications...", color = ZenithTextSubtle, fontSize = 13.5.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = ZenithTextMuted, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        AnimatedVisibility(visible = searchQuery.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = ZenithTextMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF131D31),
                        unfocusedContainerColor = Color(0xFF131D31),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = ZenithAccent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Selection Info Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF00E676).copy(alpha = 0.12f),
                                    Color(0xFF00E5FF).copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(1.dp, ZenithAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(ZenithAccent.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = "Security", tint = ZenithAccent, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${selectedApps.size} apps protected",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Screen locks automatically when launched",
                                color = ZenithTextMuted,
                                fontSize = 11.5.sp
                            )
                        }

                        if (selectedApps.isNotEmpty()) {
                            Text(
                                text = "Clear",
                                color = Color(0xFFFF5252),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedApps.clear() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isLoading) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ZenithAccent, modifier = Modifier.size(36.dp), strokeWidth = 3.dp)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            val isSelected = selectedApps.contains(app.packageName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF162544).copy(alpha = 0.5f) else Color.Transparent)
                                    .clickable {
                                        if (isSelected) {
                                            selectedApps.remove(app.packageName)
                                        } else {
                                            selectedApps.add(app.packageName)
                                        }
                                    }
                                    .padding(vertical = 9.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                app.iconBitmap?.let { bitmap ->
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = app.appName,
                                        modifier = Modifier.size(36.dp)
                                    )
                                } ?: Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF1E293B), CircleShape)
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Text(
                                    text = app.appName,
                                    color = if (isSelected) Color.White else ZenithTextSecondary,
                                    fontSize = 14.5.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )

                                // Custom Checkbox
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            color = if (isSelected) ZenithAccent else Color.Transparent,
                                            shape = RoundedCornerShape(7.dp)
                                        )
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isSelected) ZenithAccent else Color(0xFF334155),
                                            shape = RoundedCornerShape(7.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF030712),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ZenithTextMuted
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2F4D))
                    ) {
                        Text("Cancel", fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { onAppsSelected(selectedApps.toSet()) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ZenithAccent)
                    ) {
                        Text(
                            text = if (selectedApps.isNotEmpty()) "Save (${selectedApps.size})" else "Save",
                            color = Color(0xFF030712),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

