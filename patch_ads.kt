package com.noxscreen.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions

class UnityAdsManager(private val context: Context) : IUnityAdsInitializationListener {
    private val TAG = "UnityAdsManager"
    private val GAME_ID = "5746760"
    private val REWARDED_AD_UNIT_ID = "Rewarded_Android"
    private val INTERSTITIAL_AD_UNIT_ID = "interstitialVideo"
    private val testMode = false
    private var stopCounter = 0
    private val SHOW_INTERSTITIAL_EVERY = 1
    private var onInitComplete: (() -> Unit)? = null

    fun initialize(onComplete: (() -> Unit)? = null) {
        onInitComplete = onComplete
        if (!UnityAds.isInitialized) {
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
    }

    private fun loadAd(adUnitId: String) {
        if (!UnityAds.isInitialized) return
        UnityAds.load(adUnitId, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {
                Log.d(TAG, "Ad Loaded: $placementId")
            }
            override fun onUnityAdsFailedToLoad(placementId: String, error: UnityAds.UnityAdsLoadError, message: String) {
                Log.e(TAG, "Ad Failed to load: $placementId - $error - $message")
            }
        })
    }

    fun showRewardedAdWithWait(
        activity: Activity,
        onLoading: () -> Unit,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit
    ): () -> Unit {
        var isCancelled = false
        if (!UnityAds.isInitialized) {
            onFailed("Ads not initialized yet.")
            return { isCancelled = true }
        }
        
        if (activity.isDestroyed || activity.isFinishing) {
            onFailed("Activity is invalid.")
            return { isCancelled = true }
        }

        UnityAds.show(activity, REWARDED_AD_UNIT_ID, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                if (isCancelled) return
                onFailed("Failed to show ad. Please try again later.")
                loadAd(REWARDED_AD_UNIT_ID)
            }
            override fun onUnityAdsShowStart(placementId: String) {}
            override fun onUnityAdsShowClick(placementId: String) {}
            override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                if (isCancelled) return
                if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                    onSuccess()
                } else {
                    onFailed("Ad was not completed.")
                }
                loadAd(REWARDED_AD_UNIT_ID)
            }
        })
        
        return { isCancelled = true }
    }

    fun onStopAction(activity: Activity) {
        stopCounter++
        if (stopCounter >= SHOW_INTERSTITIAL_EVERY) {
            stopCounter = 0
            showInterstitialAd(activity)
        }
    }

    private fun showInterstitialAd(activity: Activity) {
        if (!UnityAds.isInitialized) return
        if (activity.isDestroyed || activity.isFinishing) return
        
        UnityAds.show(activity, INTERSTITIAL_AD_UNIT_ID, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                Log.e(TAG, "Interstitial Ad Failed to show: $error - $message")
            }
            override fun onUnityAdsShowStart(placementId: String) {}
            override fun onUnityAdsShowClick(placementId: String) {}
            override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                loadAd(INTERSTITIAL_AD_UNIT_ID)
            }
        })
    }
}
