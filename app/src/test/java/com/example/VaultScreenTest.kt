package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.model.VaultDocumentEntity
import com.example.ui.screens.VaultScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class VaultScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testVaultScreenEmptyStateAndOpenUploadDialog() {
        var addCalled = false
        var deleteCalled = false

        composeTestRule.setContent {
            VaultScreen(
                vaultDocs = emptyList(),
                onAddDocument = { _, _, _, _, _, _, _ -> addCalled = true },
                onDeleteDocument = { deleteCalled = true }
            )
        }
        composeTestRule.waitForIdle()

        // Verify Documents heading is visible
        composeTestRule.onNodeWithText("Documents").assertExists()
        composeTestRule.onNodeWithText("No documents stored").assertExists()

        // Click Upload button to open dialog
        composeTestRule.onNodeWithText("Upload").performClick()
        composeTestRule.waitForIdle()

        // Verify Dialog appears with single file picker button
        composeTestRule.onNodeWithText("Attach Files (Up to 4 files):", substring = true).assertExists()
        composeTestRule.onNodeWithText("Select Files / Photos (Up to 4)").assertExists()
    }

    @Test
    fun testVaultScreenWithDocuments() {
        val sampleDoc = VaultDocumentEntity(
            id = 1L,
            title = "My Resume",
            originalFileName = "resume.pdf",
            uriString = "file:///storage/resume.pdf",
            fileType = "PDF",
            category = "Personal",
            dateAddedTimestamp = System.currentTimeMillis(),
            fileSizeBytes = 102400L,
            notes = "Test notes"
        )

        composeTestRule.setContent {
            VaultScreen(
                vaultDocs = listOf(sampleDoc),
                onAddDocument = { _, _, _, _, _, _, _ -> },
                onDeleteDocument = { }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("My Resume").assertExists()
        composeTestRule.onNodeWithText("resume.pdf").assertExists()
    }

    @Test
    fun testVaultScreenWithMultipleFilesDocument() {
        val multiFileDoc = VaultDocumentEntity(
            id = 2L,
            title = "Insurance Bundle",
            originalFileName = "policy.pdf||card.jpg||receipt.png",
            uriString = "content://doc/1||content://doc/2||content://doc/3",
            fileType = "3 FILES",
            category = "Personal",
            dateAddedTimestamp = System.currentTimeMillis(),
            fileSizeBytes = 204800L,
            notes = "Health and car insurance bundle"
        )

        composeTestRule.setContent {
            VaultScreen(
                vaultDocs = listOf(multiFileDoc),
                onAddDocument = { _, _, _, _, _, _, _ -> },
                onDeleteDocument = { }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Insurance Bundle").assertExists()
        composeTestRule.onNodeWithText("3 files attached", substring = true).assertExists()
        composeTestRule.onNodeWithText("policy.pdf").assertExists()
        composeTestRule.onNodeWithText("card.jpg").assertExists()
        composeTestRule.onNodeWithText("receipt.png").assertExists()
    }
}
