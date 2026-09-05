package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.ui.LedgerViewModel
import com.example.ui.screens.MainLedgerScreen
import com.example.ui.theme.LedgerTheme
import com.example.util.NotificationHelper
import com.example.util.OrganiserStorageManager

class MainActivity : FragmentActivity() {
    private val viewModel: LedgerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this)
        // Automatically create 'My Organiser' directory structure in device local storage
        OrganiserStorageManager.initOrganiserStorage(this)

        setContent {
            LedgerTheme {
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.app_background),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        MainLedgerScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
