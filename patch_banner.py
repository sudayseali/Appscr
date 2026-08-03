import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """    Box(modifier = Modifier.fillMaxSize().background(ZenithBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 64.dp, bottom = 120.dp, start = 24.dp, end = 24.dp),"""

replacement = """    Box(modifier = Modifier.fillMaxSize().background(ZenithBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 64.dp, bottom = 120.dp, start = 24.dp, end = 24.dp),"""

target2 = """            }
        }
    }
}

@Composable
fun PowerPulseButton(onClick: () -> Unit, isRunning: Boolean = false) {"""

replacement2 = """            }
        }
        
        com.noxscreen.app.ads.UnityBannerAd(
            adUnitId = "bannerAd", // Default test placement or you can use your actual unit ID
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun PowerPulseButton(onClick: () -> Unit, isRunning: Boolean = false) {"""

if target in content and target2 in content:
    content = content.replace(target2, replacement2)
    with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Replaced!")
else:
    print("Not found")
