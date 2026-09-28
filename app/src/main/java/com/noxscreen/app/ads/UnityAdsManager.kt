package com.noxscreen.app.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import com.unity3d.services.core.log.DeviceLog
import com.unity3d.services.core.log.DeviceLogLevel
import java.net.InetAddress
import java.util.concurrent.Executors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnityAdsManager(private val context: Context) : IUnityAdsInitializationListener {

    companion object {
        private const val TAG = "UnityAdsManager"
        private const val UNITY_AUCTION_HOST = "auction-banner.unityads.unity3d.com"

        const val GAME_ID = "5990107"

        // Prioritize standard Unity LevelPlay Android Ad Unit IDs configured in Unity Dashboard
        private val REWARDED_PLACEMENTS = listOf("Rewarded_Android", "rewardedVideo")
        private val INTERSTITIAL_PLACEMENTS = listOf("Interstitial_Android", "interstitialVideo")

        private val _isInitializedFlow = MutableStateFlow(UnityAds.isInitialized)
        val isInitializedFlow: StateFlow<Boolean> = _isInitializedFlow.asStateFlow()

        @Volatile
        private var loadedRewardedPlacementId: String? = null

        @Volatile
        private var loadedInterstitialPlacementId: String? = null

        @Volatile
        private var isInitializing = false

        private val dnsExecutor = Executors.newSingleThreadExecutor()
        private val mainHandler = Handler(Looper.getMainLooper())

        /**
         * Prevents Unity Ads SDK internal DeviceLog from emitting Log.e("UnityAds", ...)
         * for normal operational events such as "No fill" or unreachable auction hosts.
         */
        fun suppressUnityInternalErrorLogs() {
            try {
                DeviceLog.setLogLevel(0)
            } catch (_: Throwable) {}

            try {
                val deviceLogClass = DeviceLog::class.java
                for (fieldName in listOf("LOG_ERROR", "LOG_WARNING", "LOG_INFO", "LOG_DEBUG")) {
                    try {
                        val field = deviceLogClass.getDeclaredField(fieldName)
                        field.isAccessible = true
                        field.setBoolean(null, false)
                    } catch (_: Throwable) {}
                }
                try {
                    val mapField = deviceLogClass.getDeclaredField("_deviceLogLevel")
                    mapField.isAccessible = true
                    val map = mapField.get(null) as? Map<*, *>
                    val methodField = DeviceLogLevel::class.java.getDeclaredField("_receivingMethodName")
                    methodField.isAccessible = true
                    map?.values?.forEach { levelObj ->
                        if (levelObj is DeviceLogLevel) {
                            methodField.set(levelObj, "d")
                        }
                    }
                } catch (_: Throwable) {}
            } catch (_: Throwable) {}
        }

        fun isNetworkAvailable(context: Context): Boolean {
            return try {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    ?: return false
                val network = cm.activeNetwork ?: return false
                val caps = cm.getNetworkCapabilities(network) ?: return false
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } catch (_: Throwable) {
                false
            }
        }

        fun isUnityEndpointReachable(context: Context): Boolean {
            if (!isNetworkAvailable(context)) return false
            return try {
                val addresses = InetAddress.getAllByName(UNITY_AUCTION_HOST)
                addresses != null && addresses.isNotEmpty()
            } catch (_: Throwable) {
                false
            }
        }
    }

    private val testMode = false

    // State for controlling interstitial frequency
    private var stopCounter = 0
    private val SHOW_INTERSTITIAL_EVERY = 1

    private val initCallbacks = mutableListOf<() -> Unit>()

    fun initialize(onComplete: (() -> Unit)? = null) {
        suppressUnityInternalErrorLogs()
        if (onComplete != null) {
            initCallbacks.add(onComplete)
        }

        if (!isNetworkAvailable(context)) {
            flushInitCallbacks()
            return
        }

        if (UnityAds.isInitialized) {
            suppressUnityInternalErrorLogs()
            _isInitializedFlow.value = true
            flushInitCallbacks()
            preloadAllAds()
            return
        }

        if (isInitializing) return
        isInitializing = true

        dnsExecutor.execute {
            val reachable = isUnityEndpointReachable(context)
            mainHandler.post {
                isInitializing = false
                if (!reachable) {
                    Log.d(TAG, "Unity Ads endpoint not reachable in current network environment; skipping init")
                    flushInitCallbacks()
                    return@post
                }
                try {
                    if (UnityAds.isInitialized) {
                        suppressUnityInternalErrorLogs()
                        _isInitializedFlow.value = true
                        flushInitCallbacks()
                        preloadAllAds()
                    } else {
                        suppressUnityInternalErrorLogs()
                        // Pass Activity context directly so Unity Ads ClientProperties.getActivity() is set for BannerView
                        UnityAds.initialize(context, GAME_ID, testMode, this)
                        suppressUnityInternalErrorLogs()
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Unity Ads initialization exception: ${e.message}")
                    flushInitCallbacks()
                }
            }
        }
    }

    private fun flushInitCallbacks() {
        val callbacks = ArrayList(initCallbacks)
        initCallbacks.clear()
        callbacks.forEach { it.invoke() }
    }

    override fun onInitializationComplete() {
        suppressUnityInternalErrorLogs()
        Log.d(TAG, "Unity Ads Initialization Complete")
        _isInitializedFlow.value = true
        preloadAllAds()
        flushInitCallbacks()
    }

    override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
        suppressUnityInternalErrorLogs()
        Log.d(TAG, "Unity Ads Initialization Failed: $error - $message")
        flushInitCallbacks()
    }

    private fun preloadAllAds() {
        suppressUnityInternalErrorLogs()
        if (!UnityAds.isInitialized || !isNetworkAvailable(context)) return
        preloadRewardedWithFallback(0)
        preloadInterstitialWithFallback(0)
    }

    private fun preloadRewardedWithFallback(
        index: Int,
        onLoaded: ((String) -> Unit)? = null,
        onAllFailed: (() -> Unit)? = null
    ) {
        suppressUnityInternalErrorLogs()
        if (!UnityAds.isInitialized || !isNetworkAvailable(context)) {
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
                    suppressUnityInternalErrorLogs()
                    Log.d(TAG, "Rewarded Ad Loaded: $loadedId")
                    loadedRewardedPlacementId = loadedId
                    onLoaded?.invoke(loadedId)
                }

                override fun onUnityAdsFailedToLoad(
                    failedId: String,
                    error: UnityAds.UnityAdsLoadError,
                    message: String
                ) {
                    suppressUnityInternalErrorLogs()
                    Log.d(TAG, "Rewarded Ad Failed to load ($failedId): $error - $message")
                    preloadRewardedWithFallback(index + 1, onLoaded, onAllFailed)
                }
            })
        } catch (e: Exception) {
            Log.d(TAG, "Rewarded Ad load exception: ${e.message}")
            preloadRewardedWithFallback(index + 1, onLoaded, onAllFailed)
        }
    }

    private fun preloadInterstitialWithFallback(index: Int) {
        suppressUnityInternalErrorLogs()
        if (!UnityAds.isInitialized || !isNetworkAvailable(context) || index >= INTERSTITIAL_PLACEMENTS.size) return
        val placementId = INTERSTITIAL_PLACEMENTS[index]
        try {
            UnityAds.load(placementId, object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(loadedId: String) {
                    suppressUnityInternalErrorLogs()
                    Log.d(TAG, "Interstitial Ad Loaded: $loadedId")
                    loadedInterstitialPlacementId = loadedId
                }

                override fun onUnityAdsFailedToLoad(
                    failedId: String,
                    error: UnityAds.UnityAdsLoadError,
                    message: String
                ) {
                    suppressUnityInternalErrorLogs()
                    Log.d(TAG, "Interstitial Ad Failed to load ($failedId): $error - $message")
                    preloadInterstitialWithFallback(index + 1)
                }
            })
        } catch (e: Exception) {
            Log.d(TAG, "Interstitial load exception: ${e.message}")
            preloadInterstitialWithFallback(index + 1)
        }
    }

    fun showMultipleRewardedAds(activity: Activity, remainingAds: Int, onComplete: () -> Unit) {
        suppressUnityInternalErrorLogs()
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
                suppressUnityInternalErrorLogs()
                Log.d(TAG, "Rewarded Ad Failed to show: $error - $message")
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
        suppressUnityInternalErrorLogs()
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
                suppressUnityInternalErrorLogs()
                Log.d(TAG, "Rewarded Ad Failed to show: $error - $message")
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
        suppressUnityInternalErrorLogs()
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
        suppressUnityInternalErrorLogs()
        UnityAds.show(activity, placementId, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(
                id: String,
                error: UnityAds.UnityAdsShowError,
                message: String
            ) {
                suppressUnityInternalErrorLogs()
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
                suppressUnityInternalErrorLogs()
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
        suppressUnityInternalErrorLogs()
        if (!UnityAds.isInitialized) return

        val placementToUse = loadedInterstitialPlacementId
        if (placementToUse == null) {
            preloadInterstitialWithFallback(0)
            return
        }
        UnityAds.show(activity, placementToUse, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(
                placementId: String,
                error: UnityAds.UnityAdsShowError,
                message: String
            ) {
                suppressUnityInternalErrorLogs()
                Log.d(TAG, "Interstitial Ad Failed to show: $error - $message")
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
                suppressUnityInternalErrorLogs()
                Log.d(TAG, "Interstitial Ad Completed with state: $state")
                preloadInterstitialWithFallback(0)
            }
        })
    }
}
