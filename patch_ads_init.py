import sys

with open('app/src/main/java/com/noxscreen/app/ads/UnityAdsManager.kt', 'r') as f:
    content = f.read()

target = """    private var isInitialized = false

    // State for controlling interstitial frequency"""

replacement = """    // State for controlling interstitial frequency"""

target2 = """    fun initialize() {
        if (!isInitialized) {
            UnityAds.initialize(context, GAME_ID, testMode, this)
        }
    }

    override fun onInitializationComplete() {
        isInitialized = true
        Log.d(TAG, "Unity Ads Initialization Complete")
        loadAd(REWARDED_AD_UNIT_ID)
        loadAd(INTERSTITIAL_AD_UNIT_ID)
    }

    override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
        Log.e(TAG, "Unity Ads Initialization Failed: $error - $message")
    }"""

replacement2 = """    private var onInitComplete: (() -> Unit)? = null
    
    fun initialize(onComplete: (() -> Unit)? = null) {
        onInitComplete = onComplete
        if (!UnityAds.isInitialized()) {
            UnityAds.initialize(context, GAME_ID, testMode, this)
        } else {
            onInitComplete?.invoke()
            onInitComplete = null
            loadAd(REWARDED_AD_UNIT_ID)
            loadAd(INTERSTITIAL_AD_UNIT_ID)
        }
    }

    override fun onInitializationComplete() {
        Log.d(TAG, "Unity Ads Initialization Complete")
        loadAd(REWARDED_AD_UNIT_ID)
        loadAd(INTERSTITIAL_AD_UNIT_ID)
        onInitComplete?.invoke()
        onInitComplete = null
    }

    override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
        Log.e(TAG, "Unity Ads Initialization Failed: $error - $message")
        onInitComplete?.invoke()
        onInitComplete = null
    }"""

content = content.replace(target, replacement)
content = content.replace(target2, replacement2)

target3 = """        if (remainingAds <= 0 || !isInitialized) {"""
replacement3 = """        if (remainingAds <= 0 || !UnityAds.isInitialized()) {"""
content = content.replace(target3, replacement3)

target4 = """        if (!isInitialized) {"""
replacement4 = """        if (!UnityAds.isInitialized()) {"""
content = content.replace(target4, replacement4)

target5 = """        if (!isInitialized) return"""
replacement5 = """        if (!UnityAds.isInitialized()) return"""
content = content.replace(target5, replacement5)

with open('app/src/main/java/com/noxscreen/app/ads/UnityAdsManager.kt', 'w') as f:
    f.write(content)

print("Replaced!")
