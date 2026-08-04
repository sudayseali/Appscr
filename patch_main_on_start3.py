import sys
import re

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

pattern = r"var hasPermission by remember \{ mutableStateOf\(checkOverlayPermission\(\)\) \}\s+val context = LocalContext\.current"
replacement = """var hasPermission by remember { mutableStateOf(checkOverlayPermission()) }
                        var hasRequestedPermissionOnStart by remember { mutableStateOf(false) }
                        
                        LaunchedEffect(hasPermission) {
                            if (!hasPermission && !hasRequestedPermissionOnStart) {
                                hasRequestedPermissionOnStart = true
                                requestOverlayPermission()
                            }
                        }
                    
                        val context = LocalContext.current"""

if re.search(pattern, content):
    content = re.sub(pattern, replacement, content)
    with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Replaced LaunchedEffect 3!")
else:
    print("Regex not found")
