package com.example

import android.app.Application
import com.example.ads.AdManager

class MyApplication : Application() {

    lateinit var adManager: AdManager
        private set

    override fun onCreate() {
        super.onCreate()
        // Initialize AdManager (App Open Ads, Rewarded Interstitials, AdMob SDK)
        adManager = AdManager.getInstance(this)
        adManager.initialize()
    }
}
