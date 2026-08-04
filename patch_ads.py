import sys

with open('app/src/main/java/com/noxscreen/app/ads/UnityAdsManager.kt', 'r') as f:
    content = f.read()

target = """    fun showRewardedAd(activity: Activity, onComplete: () -> Unit) {"""

replacement = """    fun showMultipleRewardedAds(activity: Activity, remainingAds: Int, onComplete: () -> Unit) {
        if (remainingAds <= 0 || !isInitialized) {
            onComplete()
            return
        }

        UnityAds.show(activity, REWARDED_AD_UNIT_ID, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                Log.e(TAG, "Rewarded Ad Failed to show: $error - $message")
                // On failure, continue with remaining ads (or you might want to skip the rest, but let's try to show the rest)
                showMultipleRewardedAds(activity, remainingAds - 1, onComplete)
            }

            override fun onUnityAdsShowStart(placementId: String) {
                Log.d(TAG, "Rewarded Ad Started")
            }

            override fun onUnityAdsShowClick(placementId: String) {
                Log.d(TAG, "Rewarded Ad Clicked")
            }

            override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                Log.d(TAG, "Rewarded Ad Completed with state: $state")
                loadAd(REWARDED_AD_UNIT_ID) // Preload next
                showMultipleRewardedAds(activity, remainingAds - 1, onComplete)
            }
        })
    }

    fun showRewardedAd(activity: Activity, onComplete: () -> Unit) {"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/noxscreen/app/ads/UnityAdsManager.kt', 'w') as f:
        f.write(content)
    print("Replaced!")
else:
    print("Not found")
