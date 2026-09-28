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

private const val TAG = "UnityBannerAd"

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * UnityBannerAd
 *
 * Soo bandhiga Unity Banner Ad-ka dhabta ah (320x50):
 * 1. Wuxuu ku jiraa Box cabbirkiisu yahay 320x50 oo muuqda si Unity WebView viewability check
 *    uu mar walba u guuleysto.
 * 2. Wuxuu sugayaa inta UnityAds.isInitialized uu ka noqonayo true ka hor inta uusan wicin banner.load().
 * 3. Haddii "Banner_Android" lagu waayo fill ama placement-ku yahay "banner", si toos ah ayuu
 *    ugu wareegayaa placement-ka xiga oo uu dib ugu soo ridayaa xayeysiiska.
 */
@Composable
fun UnityBannerAd(adUnitId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() } ?: return
    val isAdsInitialized by UnityAdsManager.isInitializedFlow.collectAsState()

    val placementCandidates = remember(adUnitId) {
        listOf(adUnitId, "Banner_Android", "banner", "bannerAd").distinct()
    }
    var currentCandidateIndex by remember { mutableIntStateOf(0) }
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

    // Isla marka UnityAds.initialize uu dhammaado, ku wac load() BannerView-ga
    LaunchedEffect(isAdsInitialized, currentCandidateIndex, reloadTrigger) {
        if (isAdsInitialized || UnityAds.isInitialized) {
            activeBannerView?.load()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .width(320.dp)
                .height(50.dp),
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    val placementId = placementCandidates[currentCandidateIndex.coerceIn(0, placementCandidates.lastIndex)]
                    val banner = buildUnityBannerView(
                        activity = activity,
                        placementId = placementId,
                        onFailed = {
                            mainHandler.removeCallbacksAndMessages(null)
                            if (currentCandidateIndex + 1 < placementCandidates.size) {
                                mainHandler.postDelayed({
                                    currentCandidateIndex += 1
                                }, 2000L)
                            } else {
                                mainHandler.postDelayed({
                                    currentCandidateIndex = 0
                                    reloadTrigger += 1
                                }, 15000L)
                            }
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
                    if (UnityAds.isInitialized) {
                        banner.load()
                    }
                }
            },
            update = { container ->
                val expectedPlacement = placementCandidates[currentCandidateIndex.coerceIn(0, placementCandidates.lastIndex)]
                val currentBanner = activeBannerView
                if (currentBanner == null || currentBanner.placementId != expectedPlacement) {
                    try {
                        currentBanner?.destroy()
                    } catch (_: Exception) {}
                    container.removeAllViews()
                    val newBanner = buildUnityBannerView(
                        activity = activity,
                        placementId = expectedPlacement,
                        onFailed = {
                            mainHandler.removeCallbacksAndMessages(null)
                            if (currentCandidateIndex + 1 < placementCandidates.size) {
                                mainHandler.postDelayed({
                                    currentCandidateIndex += 1
                                }, 2000L)
                            } else {
                                mainHandler.postDelayed({
                                    currentCandidateIndex = 0
                                    reloadTrigger += 1
                                }, 15000L)
                            }
                        }
                    )
                    activeBannerView = newBanner
                    container.addView(
                        newBanner,
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            Gravity.CENTER
                        )
                    )
                    if (UnityAds.isInitialized) {
                        newBanner.load()
                    }
                }
            }
        )
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
                Log.d(TAG, "Unity Banner Loaded: $placementId")
            }

            override fun onBannerShown(bannerView: BannerView) {
                Log.d(TAG, "Unity Banner Shown: $placementId")
            }

            override fun onBannerClick(bannerView: BannerView) {
                Log.d(TAG, "Unity Banner Clicked: $placementId")
            }

            override fun onBannerFailedToLoad(bannerView: BannerView, errorInfo: BannerErrorInfo) {
                Log.w(TAG, "Unity Banner Failed ($placementId): ${errorInfo.errorCode} - ${errorInfo.errorMessage}")
                onFailed()
            }

            override fun onBannerLeftApplication(bannerView: BannerView) {}
        }
    }
}
