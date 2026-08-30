package com.noxscreen.app.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize
import com.unity3d.services.banners.BannerErrorInfo

fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

@Composable
fun UnityBannerAd(adUnitId: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(50.dp).background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            factory = { context ->
                val activity = context.findActivity()
                if (activity != null) {
                    BannerView(activity, adUnitId, UnityBannerSize(320, 50)).apply {
                        listener = object : BannerView.IListener {
                            override fun onBannerLoaded(bannerView: BannerView) {}
                            override fun onBannerClick(bannerView: BannerView) {}
                            override fun onBannerFailedToLoad(bannerView: BannerView, errorInfo: BannerErrorInfo) {}
                            override fun onBannerLeftApplication(bannerView: BannerView) {}
                            override fun onBannerShown(bannerView: BannerView) {}
                        }
                        load()
                    }
                } else {
                    if (com.noxscreen.app.BuildConfig.DEBUG) {
                        Log.w("UnityBannerAd", "Could not find Activity from context $context to attach banner ad")
                    }
                    View(context)
                }
            }
        )
    }
}
