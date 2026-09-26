package com.noxscreen.app.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize
import com.unity3d.services.banners.BannerErrorInfo

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * UnityBannerAd
 *
 * Waxay si badbaado leh u soo bandhigtaa Banner Ad.
 * Haddii uusan helin xayeysiis ("No fill for placement Banner_Android")
 * ama uu fashilmo, uma reebayo meel madow oo faaruq ah mana keenayo khalad,
 * balse wuxuu si tartiib ah u qarinayaa banner-ka (graceful collapse).
 */
@Composable
fun UnityBannerAd(adUnitId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    if (activity == null) return

    var isBannerLoaded by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isBannerLoaded,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.Transparent)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                factory = {
                    BannerView(activity, adUnitId, UnityBannerSize(320, 50)).apply {
                        listener = object : BannerView.IListener {
                            override fun onBannerLoaded(bannerView: BannerView) {
                                isBannerLoaded = true
                            }

                            override fun onBannerClick(bannerView: BannerView) {}

                            override fun onBannerFailedToLoad(bannerView: BannerView, errorInfo: BannerErrorInfo) {
                                // Gracefully hide when no fill or network error
                                isBannerLoaded = false
                            }

                            override fun onBannerLeftApplication(bannerView: BannerView) {}

                            override fun onBannerShown(bannerView: BannerView) {
                                isBannerLoaded = true
                            }
                        }
                        load()
                    }
                }
            )
        }
    }
}
