package com.noxscreen.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnityAdsManager(private val context: Context) : IUnityAdsInitializationListener {

    companion object {
        private const val TAG = "UnityAdsManager"

        const val GAME_ID = "5990107"

        // Support both legacy and Unity LevelPlay Ad Unit IDs configured in Unity Dashboard
        private val REWARDED_PLACEMENTS = listOf("rewardedVideo", "Rewarded_Android")
        private val INTERSTITIAL_PLACEMENTS = listOf("interstitialVideo", "Interstitial_Android")

        private val _isInitializedFlow = MutableStateFlow(UnityAds.isInitialized)
        val isInitializedFlow: StateFlow<Boolean> = _isInitializedFlow.asStateFlow()

        @Volatile
        private var loadedRewardedPlacementId: String? = null

        @Volatile
        private var loadedInterstitialPlacementId: String? = null
    }

    private val testMode = false

    // State for controlling interstitial frequency
    private var stopCounter = 0
    private val SHOW_INTERSTITIAL_EVERY = 1

    private val initCallbacks = mutableListOf<() -> Unit>()

    fun initialize(onComplete: (() -> Unit)? = null) {
        if (onComplete != null) {
            initCallbacks.add(onComplete)
        }

        try {
            if (UnityAds.isInitialized) {
                _isInitializedFlow.value = true
                flushInitCallbacks()
                preloadAllAds()
            } else {
                // Pass Activity context directly so Unity Ads ClientProperties.getActivity() is set for BannerView
                UnityAds.initialize(context, GAME_ID, testMode, this)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unity Ads initialization exception: ${e.message}")
            flushInitCallbacks()
        }
    }

    private fun flushInitCallbacks() {
        val callbacks = ArrayList(initCallbacks)
        initCallbacks.clear()
        callbacks.forEach { it.invoke() }
    }

    override fun onInitializationComplete() {
        Log.d(TAG, "Unity Ads Initialization Complete")
        _isInitializedFlow.value = true
        preloadAllAds()
        flushInitCallbacks()
    }

    override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
        Log.w(TAG, "Unity Ads Initialization Failed: $error - $message")
        flushInitCallbacks()
    }

    private fun preloadAllAds() {
        if (!UnityAds.isInitialized) return
        preloadRewardedWithFallback(0)
        preloadInterstitialWithFallback(0)
    }

    private fun preloadRewardedWithFallback(
        index: Int,
        onLoaded: ((String) -> Unit)? = null,
        onAllFailed: (() -> Unit)? = null
    ) {
        if (!UnityAds.isInitialized) {
            onAllFailed?.invoke()
            return
        }
        if (index >= REWARDED_PLACEMENTS.size) {
            onAllFailed?.invoke()
            return
        }
        val placementId = REWARDED_PLACEMENTS[index]
        try {
            UnityAds.load(placementId, object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(loadedId: String) {
                    Log.d(TAG, "Rewarded Ad Loaded: $loadedId")
                    loadedRewardedPlacementId = loadedId
                    onLoaded?.invoke(loadedId)
                }

                override fun onUnityAdsFailedToLoad(
                    failedId: String,
                    error: UnityAds.UnityAdsLoadError,
                    message: String
                ) {
                    Log.w(TAG, "Rewarded Ad Failed to load ($failedId): $error - $message")
                    preloadRewardedWithFallback(index + 1, onLoaded, onAllFailed)
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "Rewarded Ad load exception: ${e.message}")
            preloadRewardedWithFallback(index + 1, onLoaded, onAllFailed)
        }
    }

    private fun preloadInterstitialWithFallback(index: Int) {
        if (!UnityAds.isInitialized || index >= INTERSTITIAL_PLACEMENTS.size) return
        val placementId = INTERSTITIAL_PLACEMENTS[index]
        try {
            UnityAds.load(placementId, object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(loadedId: String) {
                    Log.d(TAG, "Interstitial Ad Loaded: $loadedId")
                    loadedInterstitialPlacementId = loadedId
                }

                override fun onUnityAdsFailedToLoad(
                    failedId: String,
                    error: UnityAds.UnityAdsLoadError,
                    message: String
                ) {
                    Log.w(TAG, "Interstitial Ad Failed to load ($failedId): $error - $message")
                    preloadInterstitialWithFallback(index + 1)
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "Interstitial load exception: ${e.message}")
            preloadInterstitialWithFallback(index + 1)
        }
    }

    fun showMultipleRewardedAds(activity: Activity, remainingAds: Int, onComplete: () -> Unit) {
        if (remainingAds <= 0 || !UnityAds.isInitialized) {
            onComplete()
            return
        }

        val placementToUse = loadedRewardedPlacementId ?: REWARDED_PLACEMENTS.first()
        UnityAds.show(activity, placementToUse, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(
                placementId: String,
                error: UnityAds.UnityAdsShowError,
                message: String
            ) {
                Log.w(TAG, "Rewarded Ad Failed to show: $error - $message")
                showMultipleRewardedAds(activity, remainingAds - 1, onComplete)
            }

            override fun onUnityAdsShowStart(placementId: String) {
                loadedRewardedPlacementId = null
            }

            override fun onUnityAdsShowClick(placementId: String) {}

            override fun onUnityAdsShowComplete(
                placementId: String,
                state: UnityAds.UnityAdsShowCompletionState
            ) {
                preloadRewardedWithFallback(0)
                showMultipleRewardedAds(activity, remainingAds - 1, onComplete)
            }
        })
    }

    fun showRewardedAd(activity: Activity, onComplete: () -> Unit) {
        if (!UnityAds.isInitialized) {
            onComplete()
            return
        }

        val placementToUse = loadedRewardedPlacementId ?: REWARDED_PLACEMENTS.first()
        UnityAds.show(activity, placementToUse, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(
                placementId: String,
                error: UnityAds.UnityAdsShowError,
                message: String
            ) {
                Log.w(TAG, "Rewarded Ad Failed to show: $error - $message")
                onComplete()
            }

            override fun onUnityAdsShowStart(placementId: String) {
                loadedRewardedPlacementId = null
            }

            override fun onUnityAdsShowClick(placementId: String) {}

            override fun onUnityAdsShowComplete(
                placementId: String,
                state: UnityAds.UnityAdsShowCompletionState
            ) {
                onComplete()
                preloadRewardedWithFallback(0)
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

        val loadAndShow = {
            val readyPlacement = loadedRewardedPlacementId
            if (readyPlacement != null) {
                showLoadedRewardedAd(
                    activity = activity,
                    placementId = readyPlacement,
                    isCancelled = { isCancelled },
                    onSuccess = onSuccess,
                    onFailed = {
                        onLoading()
                        preloadRewardedWithFallback(
                            index = 0,
                            onLoaded = { freshId ->
                                if (!isCancelled) {
                                    showLoadedRewardedAd(
                                        activity = activity,
                                        placementId = freshId,
                                        isCancelled = { isCancelled },
                                        onSuccess = onSuccess,
                                        onFailed = onFailed
                                    )
                                }
                            },
                            onAllFailed = {
                                if (!isCancelled) {
                                    onFailed("Failed to load ad. Please check your internet connection and try again.")
                                }
                            }
                        )
                    }
                )
            } else {
                onLoading()
                preloadRewardedWithFallback(
                    index = 0,
                    onLoaded = { freshId ->
                        if (!isCancelled) {
                            showLoadedRewardedAd(
                                activity = activity,
                                placementId = freshId,
                                isCancelled = { isCancelled },
                                onSuccess = onSuccess,
                                onFailed = onFailed
                            )
                        }
                    },
                    onAllFailed = {
                        if (!isCancelled) {
                            onFailed("Failed to load ad. Please check your internet connection and try again.")
                        }
                    }
                )
            }
        }

        if (!UnityAds.isInitialized) {
            onLoading()
            initialize {
                if (!isCancelled) {
                    if (UnityAds.isInitialized) {
                        loadAndShow()
                    } else {
                        onFailed("Ads service could not initialize. Please check your internet connection.")
                    }
                }
            }
        } else {
            loadAndShow()
        }

        return { isCancelled = true }
    }

    private fun showLoadedRewardedAd(
        activity: Activity,
        placementId: String,
        isCancelled: () -> Boolean,
        onSuccess: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        UnityAds.show(activity, placementId, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(
                id: String,
                error: UnityAds.UnityAdsShowError,
                message: String
            ) {
                loadedRewardedPlacementId = null
                if (!isCancelled()) {
                    onFailed("Failed to show ad. Please try again.")
                }
            }

            override fun onUnityAdsShowStart(id: String) {
                loadedRewardedPlacementId = null
            }

            override fun onUnityAdsShowClick(id: String) {}

            override fun onUnityAdsShowComplete(
                id: String,
                state: UnityAds.UnityAdsShowCompletionState
            ) {
                if (!isCancelled()) {
                    if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                        onSuccess()
                    } else {
                        onFailed("Ad was not completed.")
                    }
                }
                preloadRewardedWithFallback(0)
            }
        })
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

        val placementToUse = loadedInterstitialPlacementId ?: INTERSTITIAL_PLACEMENTS.first()
        UnityAds.show(activity, placementToUse, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(
                placementId: String,
                error: UnityAds.UnityAdsShowError,
                message: String
            ) {
                Log.w(TAG, "Interstitial Ad Failed to show: $error - $message")
                loadedInterstitialPlacementId = null
                preloadInterstitialWithFallback(0)
            }

            override fun onUnityAdsShowStart(placementId: String) {
                Log.d(TAG, "Interstitial Ad Started")
                loadedInterstitialPlacementId = null
            }

            override fun onUnityAdsShowClick(placementId: String) {}

            override fun onUnityAdsShowComplete(
                placementId: String,
                state: UnityAds.UnityAdsShowCompletionState
            ) {
                Log.d(TAG, "Interstitial Ad Completed with state: $state")
                preloadInterstitialWithFallback(0)
            }
        })
    }
}
