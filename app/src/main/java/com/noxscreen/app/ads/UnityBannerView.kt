package com.noxscreen.app.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.unity3d.ads.UnityAds
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize
import java.net.InetAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "UnityBannerAd"
private const val BANNER_AUCTION_HOST = "auction-banner.unityads.unity3d.com"

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

internal fun canResolveBannerHost(context: Context): Boolean {
    if (!UnityAdsManager.isNetworkAvailable(context)) return false
    return try {
        val addresses = InetAddress.getAllByName(BANNER_AUCTION_HOST)
        addresses != null && addresses.isNotEmpty()
    } catch (_: Throwable) {
        false
    }
}

/**
 * UnityBannerAd
 *
 * Displays a 320x50 Unity Banner Ad:
 * 1. Waits until UnityAds is initialized and verifies that the Unity banner auction host is
 *    reachable/resolvable before instantiating BannerView or calling load().
 * 2. Calls banner.load() once per cycle without duplicate concurrent calls or rapid WebView churn.
 */
@Composable
fun UnityBannerAd(adUnitId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() } ?: return
    val isAdsInitialized by UnityAdsManager.isInitializedFlow.collectAsState()

    var isBannerReachable by remember { mutableStateOf(false) }
    var reloadTrigger by remember { mutableIntStateOf(0) }
    var activeBannerView by remember { mutableStateOf<BannerView?>(null) }

    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    DisposableEffect(Unit) {
        onDispose {
            mainHandler.removeCallbacksAndMessages(null)
            try {
                activeBannerView?.destroy()
            } catch (_: Exception) {}
            activeBannerView = null
        }
    }

    LaunchedEffect(isAdsInitialized, reloadTrigger) {
        UnityAdsManager.suppressUnityInternalErrorLogs()
        if (isAdsInitialized || UnityAds.isInitialized) {
            val reachable = withContext(Dispatchers.IO) {
                canResolveBannerHost(context)
            }
            isBannerReachable = reachable
            if (reachable && reloadTrigger > 0) {
                UnityAdsManager.suppressUnityInternalErrorLogs()
                activeBannerView?.load()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (isBannerReachable && (isAdsInitialized || UnityAds.isInitialized)) {
            AndroidView(
                modifier = Modifier
                    .width(320.dp)
                    .height(50.dp),
                factory = { ctx ->
                    FrameLayout(ctx).apply {
                        UnityAdsManager.suppressUnityInternalErrorLogs()
                        val banner = buildUnityBannerView(
                            activity = activity,
                            placementId = adUnitId,
                            onFailed = {
                                UnityAdsManager.suppressUnityInternalErrorLogs()
                                mainHandler.removeCallbacksAndMessages(null)
                                mainHandler.postDelayed({
                                    reloadTrigger += 1
                                }, 60000L)
                            }
                        )
                        activeBannerView = banner
                        addView(
                            banner,
                            FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                Gravity.CENTER
                            )
                        )
                        banner.load()
                    }
                }
            )
        }
    }
}

private fun buildUnityBannerView(
    activity: Activity,
    placementId: String,
    onFailed: () -> Unit
): BannerView {
    return BannerView(activity, placementId, UnityBannerSize(320, 50)).apply {
        listener = object : BannerView.IListener {
            override fun onBannerLoaded(bannerView: BannerView) {
                UnityAdsManager.suppressUnityInternalErrorLogs()
                Log.d(TAG, "Unity Banner Loaded: $placementId")
            }

            override fun onBannerShown(bannerView: BannerView) {
                UnityAdsManager.suppressUnityInternalErrorLogs()
                Log.d(TAG, "Unity Banner Shown: $placementId")
            }

            override fun onBannerClick(bannerView: BannerView) {
                Log.d(TAG, "Unity Banner Clicked: $placementId")
            }

            override fun onBannerFailedToLoad(bannerView: BannerView, errorInfo: BannerErrorInfo) {
                UnityAdsManager.suppressUnityInternalErrorLogs()
                Log.d(TAG, "Unity Banner Failed ($placementId): ${errorInfo.errorCode} - ${errorInfo.errorMessage}")
                onFailed()
            }

            override fun onBannerLeftApplication(bannerView: BannerView) {}
        }
    }
}
