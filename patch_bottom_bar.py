import re

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "r") as f:
    content = f.read()

target = """        com.noxscreen.app.ads.UnityBannerAd(
            adUnitId = "Banner_Android", // Default test placement or you can use your actual unit ID
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}"""

replacement = """        // Bottom controls
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DarkMode, contentDescription = "Dark Mode", tint = Color.White, modifier = Modifier.size(24.dp))
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Brush.radialGradient(listOf(ZenithAccent.copy(alpha=0.3f), Color.Transparent)), CircleShape)
                            .border(1.dp, ZenithAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock", tint = ZenithAccent)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.KeyboardDoubleArrowLeft, contentDescription = null, tint = ZenithAccent.copy(alpha=0.5f), modifier = Modifier.size(16.dp))
                        Text(" Drag to move ", color = ZenithAccent, fontSize = 12.sp)
                        Icon(Icons.Default.KeyboardDoubleArrowRight, contentDescription = null, tint = ZenithAccent.copy(alpha=0.5f), modifier = Modifier.size(16.dp))
                    }
                }
                
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .background(ZenithCard, RoundedCornerShape(24.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Eco, contentDescription = "Eco", tint = ZenithAccent, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Thank you for saving energy and", color = ZenithTextMuted, fontSize = 12.sp)
                    Text("extending your screen life.", color = ZenithTextMuted, fontSize = 12.sp)
                }
                Icon(Icons.Default.Favorite, contentDescription = "Heart", tint = ZenithAccent, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            com.noxscreen.app.ads.UnityBannerAd(
                adUnitId = "Banner_Android",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/noxscreen/app/MainActivity.kt", "w") as f:
    f.write(content)
