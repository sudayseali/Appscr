import re

file_path = "app/src/main/java/com/noxscreen/app/MainActivity.kt"

with open(file_path, "r") as f:
    code = f.read()

# Target pattern from Column(modifier = Modifier.align(Alignment.BottomCenter)...) to end of Eco Thank You Card
dock_pattern = re.compile(
    r'(\s*// Bottom Dock Controls\s*Column\([\s\S]*?Spacer\(modifier = Modifier\.height\(10\.dp\)\))',
    re.MULTILINE
)

# Replace the quick controls and eco card, keeping only UnityBannerAd if present or removing the dock clutter
replacement = """
        // Bottom Banner Ad
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            com.noxscreen.app.ads.UnityBannerAd(
                adUnitId = "Banner_Android",
                modifier = Modifier.fillMaxWidth()
            )
        }"""

if dock_pattern.search(code):
    code = dock_pattern.sub(replacement, code)
    print("Found and replaced bottom dock controls successfully!")
else:
    print("Pattern not found, checking exact text match...")

with open(file_path, "w") as f:
    f.write(code)

