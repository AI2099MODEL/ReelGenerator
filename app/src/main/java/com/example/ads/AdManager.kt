package com.example.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AdManager coordinates App Open Ads, Rewarded Interstitial Ads,
 * and 10-Minute Periodic Interstitial transitions in strict compliance with Google AdMob Policies.
 */
class AdManager(private val application: Application) :
    Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    companion object {
        private const val TAG = "AdManager"

        // Production Ad Unit IDs provided by user
        const val APP_ID = "ca-app-pub-8815300826143812~6360612912"
        const val BANNER_AD_UNIT_ID = "ca-app-pub-8815300826143812/1631030419"
        const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-8815300826143812/2728489663"
        const val REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-8815300826143812/1452056697"

        // Periodic intervals and cooldowns
        private const val TEN_MINUTES_MS = 10 * 60 * 1000L
        private const val APP_OPEN_COOLDOWN_MS = 4 * 60 * 1000L // 4 minutes minimum between App Open Ads
        private const val MIN_BACKGROUND_TIME_MS = 30 * 1000L  // App must be backgrounded for 30s to trigger app open on return

        @Volatile
        private var INSTANCE: AdManager? = null

        fun getInstance(application: Application): AdManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdManager(application).also { INSTANCE = it }
            }
        }

        fun get(): AdManager? = INSTANCE
    }

    private var currentActivity: Activity? = null

    // App Open Ad variables
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAppOpenAd = false
    private var isShowingAppOpenAd = false
    private var appOpenLoadTime: Long = 0
    private var lastAppOpenShownTime: Long = 0
    private var appBackgroundedTime: Long = 0
    private var hasShownInitialAppOpen = false

    // Rewarded Interstitial Ad variables
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var isLoadingRewardedInterstitial = false
    private var isShowingRewardedInterstitial = false

    // State of Image Studio Unlock
    private val _isImageStudioUnlocked = MutableStateFlow(false)
    val isImageStudioUnlocked: StateFlow<Boolean> = _isImageStudioUnlocked.asStateFlow()

    // 10-Minute Periodic Interstitial tracking
    private var lastPeriodicAdShownTime: Long = System.currentTimeMillis()

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun initialize() {
        MobileAds.initialize(application) {
            Log.d(TAG, "Google Mobile Ads SDK Initialized")
            loadAppOpenAd()
            loadRewardedInterstitialAd()
        }
    }

    // ==========================================
    // 1. APP OPEN AD IMPLEMENTATION
    // ==========================================

    fun loadAppOpenAd() {
        if (isLoadingAppOpenAd || isAppOpenAdAvailable()) {
            return
        }

        isLoadingAppOpenAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            application,
            APP_OPEN_AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "App Open Ad loaded into cache.")
                    appOpenAd = ad
                    isLoadingAppOpenAd = false
                    appOpenLoadTime = Date().time

                    // Show once on initial cold launch if not shown yet
                    if (!hasShownInitialAppOpen && !isShowingAppOpenAd && !isShowingRewardedInterstitial) {
                        currentActivity?.let { act ->
                            hasShownInitialAppOpen = true
                            showAppOpenAdIfAvailable(act)
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "App Open Ad failed to load: ${loadAdError.message}")
                    isLoadingAppOpenAd = false
                    appOpenAd = null
                }
            }
        )
    }

    private fun isAppOpenAdAvailable(): Boolean {
        val wasLessThan4Hours = (Date().time - appOpenLoadTime) < (4 * 3600 * 1000)
        return appOpenAd != null && wasLessThan4Hours
    }

    fun showAppOpenAdIfAvailable(activity: Activity, onComplete: () -> Unit = {}) {
        // Prevent multiple simultaneous ads or rapid looping
        if (isShowingAppOpenAd || isShowingRewardedInterstitial) {
            onComplete()
            return
        }

        val currentTime = System.currentTimeMillis()
        if (lastAppOpenShownTime > 0 && (currentTime - lastAppOpenShownTime) < APP_OPEN_COOLDOWN_MS) {
            Log.d(TAG, "App Open Ad skipped due to cooldown policy.")
            onComplete()
            return
        }

        if (!isAppOpenAdAvailable()) {
            loadAppOpenAd()
            onComplete()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "App Open Ad dismissed.")
                appOpenAd = null
                isShowingAppOpenAd = false
                lastAppOpenShownTime = System.currentTimeMillis()
                onComplete()
                // Preload next ad without showing it
                loadAppOpenAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "App Open Ad failed to show: ${adError.message}")
                appOpenAd = null
                isShowingAppOpenAd = false
                onComplete()
                loadAppOpenAd()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "App Open Ad showed full screen.")
                isShowingAppOpenAd = true
                lastAppOpenShownTime = System.currentTimeMillis()
            }
        }

        isShowingAppOpenAd = true
        appOpenAd?.show(activity)
    }

    // ==========================================
    // 2. REWARDED INTERSTITIAL AD IMPLEMENTATION
    // ==========================================

    fun loadRewardedInterstitialAd() {
        if (isLoadingRewardedInterstitial || rewardedInterstitialAd != null) {
            return
        }

        isLoadingRewardedInterstitial = true
        val request = AdRequest.Builder().build()
        RewardedInterstitialAd.load(
            application,
            REWARDED_INTERSTITIAL_AD_UNIT_ID,
            request,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    Log.d(TAG, "Rewarded Interstitial Ad loaded successfully.")
                    rewardedInterstitialAd = ad
                    isLoadingRewardedInterstitial = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Rewarded Interstitial Ad failed to load: ${loadAdError.message}")
                    isLoadingRewardedInterstitial = false
                    rewardedInterstitialAd = null
                }
            }
        )
    }

    fun isRewardedInterstitialAdLoaded(): Boolean {
        return rewardedInterstitialAd != null
    }

    fun showRewardedInterstitialAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onDismissedOrFailed: () -> Unit
    ) {
        val ad = rewardedInterstitialAd
        if (ad == null) {
            // If ad is not ready yet, load for next time and gracefully grant access
            loadRewardedInterstitialAd()
            _isImageStudioUnlocked.value = true
            onRewardEarned()
            return
        }

        isShowingRewardedInterstitial = true
        var userEarnedReward = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded Interstitial Ad dismissed.")
                rewardedInterstitialAd = null
                isShowingRewardedInterstitial = false
                lastPeriodicAdShownTime = System.currentTimeMillis()
                lastAppOpenShownTime = System.currentTimeMillis()
                loadRewardedInterstitialAd()
                if (userEarnedReward) {
                    _isImageStudioUnlocked.value = true
                    onRewardEarned()
                } else {
                    onDismissedOrFailed()
                }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Rewarded Interstitial Ad failed to show: ${adError.message}")
                rewardedInterstitialAd = null
                isShowingRewardedInterstitial = false
                loadRewardedInterstitialAd()
                // Graceful fallback
                _isImageStudioUnlocked.value = true
                onRewardEarned()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded Interstitial Ad displayed.")
            }
        }

        ad.show(activity) { rewardItem ->
            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            userEarnedReward = true
            _isImageStudioUnlocked.value = true
        }
    }

    fun unlockImageStudioDirectly() {
        _isImageStudioUnlocked.value = true
    }

    // ==========================================
    // 3. 10-MINUTE PERIODIC INTERSTITIAL
    // ==========================================

    fun checkAndShowPeriodicInterstitial(activity: Activity) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPeriodicAdShownTime >= TEN_MINUTES_MS) {
            if (!isShowingAppOpenAd && !isShowingRewardedInterstitial) {
                lastPeriodicAdShownTime = currentTime
                if (rewardedInterstitialAd != null) {
                    Log.d(TAG, "10-Minute interval reached. Showing transition ad.")
                    showRewardedInterstitialAd(
                        activity = activity,
                        onRewardEarned = {},
                        onDismissedOrFailed = {}
                    )
                }
            }
        }
    }

    // ==========================================
    // LIFECYCLE CALLBACKS (ProcessLifecycleOwner)
    // ==========================================

    override fun onStart(owner: LifecycleOwner) {
        // Called when the entire application comes to the foreground
        val timeInBackground = System.currentTimeMillis() - appBackgroundedTime
        if (appBackgroundedTime > 0 && timeInBackground >= MIN_BACKGROUND_TIME_MS) {
            currentActivity?.let { activity ->
                showAppOpenAdIfAvailable(activity)
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        // App went to the background
        appBackgroundedTime = System.currentTimeMillis()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        if (!isShowingAppOpenAd && !isShowingRewardedInterstitial) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
