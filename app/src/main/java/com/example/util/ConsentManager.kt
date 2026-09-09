package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * Manages Google User Messaging Platform (UMP) consent flows to guarantee
 * full compliance with Google Play Ads policies, EMEA regulations, and GDPR (EU/UK).
 */
object ConsentManager {
    private const val TAG = "ConsentManager"

    /**
     * Request and gather consent info for EEA/UK and global users.
     */
    fun requestConsent(
        activity: Activity,
        onConsentGathered: (canRequestAds: Boolean) -> Unit
    ) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { loadAndShowError ->
                    if (loadAndShowError != null) {
                        Log.w(TAG, "Consent form error: ${loadAndShowError.errorCode}: ${loadAndShowError.message}")
                    }
                    onConsentGathered(consentInformation.canRequestAds())
                }
            },
            { requestConsentError ->
                Log.w(TAG, "Consent info update failed: ${requestConsentError.errorCode}: ${requestConsentError.message}")
                onConsentGathered(consentInformation.canRequestAds())
            }
        )
    }

    /**
     * Check if consent status allows ad requests.
     */
    fun canRequestAds(context: Context): Boolean {
        return UserMessagingPlatform.getConsentInformation(context).canRequestAds()
    }
}
