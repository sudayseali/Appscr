import sys

with open('app/src/main/java/com/noxscreen/app/ads/UnityAdsManager.kt', 'r') as f:
    content = f.read()

content = content.replace("!UnityAds.isInitialized()", "!UnityAds.isInitialized")

with open('app/src/main/java/com/noxscreen/app/ads/UnityAdsManager.kt', 'w') as f:
    f.write(content)

print("Replaced!")
