import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target1 = """                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF050510)
                ) {
                    var hasPermission by remember { mutableStateOf(checkOverlayPermission()) }"""

replacement1 = """                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF050510)
                ) {
                    var showSplash by remember { mutableStateOf(true) }
                    
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(2500)
                        showSplash = false
                    }
                    
                    if (showSplash) {
                        SplashScreen()
                    } else {
                        var hasPermission by remember { mutableStateOf(checkOverlayPermission()) }"""

content = content.replace(target1, replacement1)

target2 = """                        totalTimeSaved = totalTimeSaved,
                        usageCount = usageCount
                    )
                }
            }
        }
    }"""

replacement2 = """                        totalTimeSaved = totalTimeSaved,
                        usageCount = usageCount
                    )
                    }
                }
            }
        }
    }"""

content = content.replace(target2, replacement2)

splash_composable = """
@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZenithBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.NightsStay,
                contentDescription = "App Icon",
                tint = ZenithAccent,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "NOXSCREEN",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
            Text(
                text = "PRO",
                color = ZenithAccent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 8.sp
            )
            Spacer(modifier = Modifier.height(64.dp))
            CircularProgressIndicator(
                color = ZenithAccent,
                modifier = Modifier.size(36.dp),
                strokeWidth = 3.dp
            )
        }
    }
}
"""

content += splash_composable

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
    f.write(content)

