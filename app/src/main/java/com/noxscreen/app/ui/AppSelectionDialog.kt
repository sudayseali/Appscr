package com.noxscreen.app.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    val icon: Drawable?
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
    var selectedApps by remember { mutableStateOf(initialSelectedApps.toMutableSet()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val apps = packages.filter {
                pm.getLaunchIntentForPackage(it.packageName) != null && it.packageName != context.packageName
            }.map {
                AppItem(
                    packageName = it.packageName,
                    appName = pm.getApplicationLabel(it).toString(),
                    icon = pm.getApplicationIcon(it)
                )
            }.sortedBy { it.appName.lowercase() }
            
            withContext(Dispatchers.Main) {
                installedApps = apps
                isLoading = false
            }
        }
    }

    val filteredApps = installedApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color(0xFF0F172A), // Dark blue-gray like screenshot
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Top handle indicator
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.Gray.copy(alpha = 0.5f), CircleShape)
                        .align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Select Apps to Block",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Search & Filter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        placeholder = { Text("Search apps...", color = Color.Gray, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = ZenithAccent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Filter button
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                            .clickable { /* Filter logic */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Filter", tint = ZenithAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Filter", color = ZenithAccent, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Selection Info Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF064E3B).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF059669).copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = "Security", tint = ZenithAccent)
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${selectedApps.size} apps selected",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "These apps will be blocked in Focus Mode",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        
                        Text(
                            text = "Clear All",
                            color = ZenithAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { selectedApps.clear() }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isLoading) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ZenithAccent)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filteredApps) { app ->
                            val isSelected = selectedApps.contains(app.packageName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (isSelected) {
                                            selectedApps.remove(app.packageName)
                                        } else {
                                            selectedApps.add(app.packageName)
                                        }
                                        selectedApps = selectedApps.toMutableSet()
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                app.icon?.let { drawable ->
                                    Image(
                                        bitmap = drawable.toBitmap().asImageBitmap(),
                                        contentDescription = app.appName,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = app.appName,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Custom Checkbox
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            color = if (isSelected) ZenithAccent else Color.Transparent,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isSelected) ZenithAccent else Color.Gray.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(6.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
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
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ZenithAccent
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    
                    Button(
                        onClick = { onAppsSelected(selectedApps) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ZenithAccent)
                    ) {
                        Text("Save (${selectedApps.size})", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
