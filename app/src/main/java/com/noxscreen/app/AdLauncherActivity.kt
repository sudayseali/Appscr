package com.noxscreen.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.noxscreen.app.ads.UnityAdsManager

class AdLauncherActivity : Activity() {
    private lateinit var adsManager: UnityAdsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Transparent window
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        adsManager = UnityAdsManager(this)
        
        val adsCount = intent.getIntExtra("ADS_COUNT", 3)
        val action = intent.getStringExtra("ON_COMPLETE_ACTION") ?: "START_BLACKOUT"

        adsManager.initialize {
            adsManager.showMultipleRewardedAds(this, adsCount) {
                if (action == "START_BLACKOUT") {
                    val intent = Intent(this, BlackScreenService::class.java).apply {
                        this.action = "START_BLACKOUT"
                    }
                    startService(intent)
                    val broadcastIntent = Intent("com.noxscreen.app.START_BLACKOUT")
                    sendBroadcast(broadcastIntent)
                } else if (action == "UNLOCK_SCREEN") {
                    val intent = Intent(this, BlackScreenService::class.java).apply {
                        this.action = "UNLOCK_SCREEN"
                    }
                    startService(intent)
                }
                finish()
            }
        }
    }
}
