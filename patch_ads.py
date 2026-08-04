import re

with open('app/src/main/java/com/noxscreen/app/ads/UnityAdsManager.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Change SHOW_INTERSTITIAL_EVERY to 1 so it always shows on stop
content = content.replace("private val SHOW_INTERSTITIAL_EVERY = 3", "private val SHOW_INTERSTITIAL_EVERY = 1")

# Add showRewardedAdWithWait
new_method = """
    fun showRewardedAdWithWait(
        activity: Activity,
        onLoading: () -> Unit,
        onSuccess: () -> Unit,
        onFailed: () -> Unit
    ) {
        if (!UnityAds.isInitialized) {
            onFailed()
            return
        }

        UnityAds.show(activity, REWARDED_AD_UNIT_ID, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                onLoading()
                UnityAds.load(REWARDED_AD_UNIT_ID, object : IUnityAdsLoadListener {
                    override fun onUnityAdsAdLoaded(placementId: String) {
                        UnityAds.show(activity, REWARDED_AD_UNIT_ID, UnityAdsShowOptions(), object : IUnityAdsShowListener {
                            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                                onFailed()
                            }
                            override fun onUnityAdsShowStart(placementId: String) {}
                            override fun onUnityAdsShowClick(placementId: String) {}
                            override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                                if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) onSuccess() else onFailed()
                                loadAd(REWARDED_AD_UNIT_ID)
                            }
                        })
                    }
                    override fun onUnityAdsFailedToLoad(placementId: String, error: UnityAds.UnityAdsLoadError, message: String) {
                        onFailed()
                    }
                })
            }
            override fun onUnityAdsShowStart(placementId: String) {}
            override fun onUnityAdsShowClick(placementId: String) {}
            override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) onSuccess() else onFailed()
                loadAd(REWARDED_AD_UNIT_ID)
            }
        })
    }
"""

if "showRewardedAdWithWait" not in content:
    content = content.replace("fun onStopAction(activity: Activity) {", new_method + "\n    fun onStopAction(activity: Activity) {")

with open('app/src/main/java/com/noxscreen/app/ads/UnityAdsManager.kt', 'w', encoding='utf-8') as f:
    f.write(content)
