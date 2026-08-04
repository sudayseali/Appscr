import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """                    } else {
                        var hasPermission by remember { mutableStateOf(checkOverlayPermission()) }
                    
                        val context = LocalContext.current"""

replacement = """                    } else {
                        var hasPermission by remember { mutableStateOf(checkOverlayPermission()) }
                        var hasRequestedPermissionOnStart by remember { mutableStateOf(false) }
                        
                        LaunchedEffect(hasPermission) {
                            if (!hasPermission && !hasRequestedPermissionOnStart) {
                                hasRequestedPermissionOnStart = true
                                requestOverlayPermission()
                            }
                        }
                    
                        val context = LocalContext.current"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Replaced LaunchedEffect 2!")
else:
    print("Target still not found")
