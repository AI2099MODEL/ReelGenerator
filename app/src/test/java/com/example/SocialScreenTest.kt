package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.screens.SocialScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class SocialScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSocialScreenRenders() {
        composeTestRule.setContent {
            SocialScreen()
        }
        println(composeTestRule.onRoot().printToString())
    }
}
