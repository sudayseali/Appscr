import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """                        onStartService = { 
                            // Show 3 rewarded ads, then start black screen
                            adsManager.showMultipleRewardedAds(this@MainActivity, 3) {
                                startBlackScreenService()
                                isServiceRunning = true
                            }
                        },"""
replacement = """                        onStartService = { 
                            startBlackScreenService()
                            isServiceRunning = true
                        },"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Reverted Main ads!")
else:
    print("Main ads target not found")
