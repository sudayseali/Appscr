import sys

with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'r') as f:
    content = f.read()

target = """                            // Show rewarded ad, then start black screen
                            adsManager.showRewardedAd(this@MainActivity) {
                                startBlackScreenService()
                                isServiceRunning = true
                            }"""

replacement = """                            // Show 3 rewarded ads, then start black screen
                            adsManager.showMultipleRewardedAds(this@MainActivity, 3) {
                                startBlackScreenService()
                                isServiceRunning = true
                            }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Replaced MainActivity ads!")
else:
    print("Target not found in MainActivity")
