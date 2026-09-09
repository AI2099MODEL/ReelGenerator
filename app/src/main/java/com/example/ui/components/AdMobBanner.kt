package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * AdMob Banner Ad View Composable adhering to Google Play & AdMob policies.
 * Fixed 320x50dp dimension ensures it never stretches or covers adjacent tab bars.
 */
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-8815300826143812/1631030419"
) {
    val isInEditMode = LocalInspectionMode.current
    if (isInEditMode) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .width(320.dp)
                .height(50.dp),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    if (com.example.util.ConsentManager.canRequestAds(context)) {
                        loadAd(AdRequest.Builder().build())
                    }
                }
            }
        )
    }
}

