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
    
    // Replace these with your actual Unity Game ID and Ad Unit IDs
    private val GAME_ID = "5990107"
    private val REWARDED_AD_UNIT_ID = "rewardedVideo"
    private val INTERSTITIAL_AD_UNIT_ID = "interstitialVideo"
    private val testMode = true

    // State for controlling interstitial frequency
    private var stopCounter = 0
    private val SHOW_INTERSTITIAL_EVERY = 3

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

    /**
     * Shows a rewarded ad. Triggers [onComplete] regardless of whether the ad was watched successfully, skipped, or failed.
     * This ensures the app flow is not blocked.
     */
    fun showMultipleRewardedAds(activity: Activity, remainingAds: Int, onComplete: () -> Unit) {
        if (remainingAds <= 0 || !UnityAds.isInitialized) {
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

    fun showRewardedAd(activity: Activity, onComplete: () -> Unit) {
        if (!UnityAds.isInitialized) {
            onComplete()
            return
        }
        
        UnityAds.show(activity, REWARDED_AD_UNIT_ID, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                Log.e(TAG, "Rewarded Ad Failed to show: $error - $message")
                onComplete()
            }

            override fun onUnityAdsShowStart(placementId: String) {
                Log.d(TAG, "Rewarded Ad Started")
            }

            override fun onUnityAdsShowClick(placementId: String) {
                Log.d(TAG, "Rewarded Ad Clicked")
            }

            override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                Log.d(TAG, "Rewarded Ad Completed with state: $state")
                // We proceed whether they completed it or skipped it.
                onComplete()
                // Preload the next one
                loadAd(REWARDED_AD_UNIT_ID)
            }
        })
    }

    /**
     * Called when the user stops the black screen. Shows an interstitial every 3rd time.
     */
    fun onStopAction(activity: Activity) {
        stopCounter++
        if (stopCounter >= SHOW_INTERSTITIAL_EVERY) {
            stopCounter = 0
            showInterstitialAd(activity)
        }
    }

    private fun showInterstitialAd(activity: Activity) {
        if (!UnityAds.isInitialized) return

        UnityAds.show(activity, INTERSTITIAL_AD_UNIT_ID, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                Log.e(TAG, "Interstitial Ad Failed to show: $error - $message")
            }

            override fun onUnityAdsShowStart(placementId: String) {
                Log.d(TAG, "Interstitial Ad Started")
            }

            override fun onUnityAdsShowClick(placementId: String) {
                Log.d(TAG, "Interstitial Ad Clicked")
            }

            override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                Log.d(TAG, "Interstitial Ad Completed with state: $state")
                loadAd(INTERSTITIAL_AD_UNIT_ID)
            }
        })
    }
}
