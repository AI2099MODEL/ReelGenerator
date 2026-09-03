package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.LedgerViewModel
import com.example.ui.screens.MainLedgerScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class MainLedgerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testMainLedgerScreenRenders() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LedgerViewModel(app)
        composeTestRule.setContent {
            MainLedgerScreen(viewModel = viewModel)
        }
        composeTestRule.waitForIdle()
    }
}
