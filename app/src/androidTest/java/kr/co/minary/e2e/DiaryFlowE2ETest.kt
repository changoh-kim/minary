package kr.co.minary.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.HiltAndroidTest
import kr.co.minary.testing.BaseE2ETest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import kr.co.presentation.R as PresentationR

@LargeTest
@HiltAndroidTest
class DiaryFlowE2ETest : BaseE2ETest() {

    @Test
    fun createDiary_savesEntryAndOpensDetail() {
        fakeBackend.signInAsTestUser()

        launchApp()

        composeRule.onNodeWithTag("calendar_new_diary_button").performClick()
        composeRule.onNodeWithTag("diary_edit_title_field").performTextInput("created-title")
        composeRule.onNodeWithTag("diary_edit_content_field").performTextInput("created-content")
        composeRule.onNodeWithTag("diary_edit_save_button").performClick()

        composeRule.onNodeWithText("created-title").assertIsDisplayed()
        assertEquals("created-content", fakeBackend.diaryByTitle("created-title")?.content)
    }

    @Test
    fun editDiary_updatesEntryAndReturnsToDetail() {
        val diary = fakeBackend.seedDiary(
            date = LocalDate.of(2026, 1, 15),
            title = "edit-original-title",
            content = "edit-original-content",
        )
        fakeBackend.signInAsTestUser()

        launchApp()
        openDiaryFromSearch(query = "edit", date = diary.date)

        composeRule
            .onNodeWithContentDescription(text(PresentationR.string.diary_edit_button))
            .performClick()
        composeRule.onNodeWithTag("diary_edit_title_field").performTextClearance()
        composeRule.onNodeWithTag("diary_edit_title_field").performTextInput("edit-updated-title")
        composeRule.onNodeWithTag("diary_edit_content_field").performTextClearance()
        composeRule.onNodeWithTag("diary_edit_content_field").performTextInput("edit-updated-content")
        composeRule.onNodeWithTag("diary_edit_save_button").performClick()

        composeRule.waitForIdle()
        composeRule.onNodeWithText("edit-updated-title").assertExists()
        assertEquals("edit-updated-content", fakeBackend.diaryByTitle("edit-updated-title")?.content)
    }

    @Test
    fun deleteDiary_removesEntryAndReturnsToSearch() {
        val diary = fakeBackend.seedDiary(
            date = LocalDate.of(2026, 1, 15),
            title = "delete-title",
            content = "delete-content",
        )
        fakeBackend.signInAsTestUser()

        launchApp()
        openDiaryFromSearch(query = "delete", date = diary.date)

        composeRule.onNodeWithText(diary.title).assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(
                label = text(PresentationR.string.profile_common_more_desc),
                useUnmergedTree = true,
            )
            .performClick()
        composeRule.onNodeWithText(text(PresentationR.string.diary_delete_menu_button)).performClick()
        composeRule.onNodeWithText(text(PresentationR.string.diary_delete_dialog_title)).assertIsDisplayed()
        composeRule
            .onNodeWithText(text(PresentationR.string.dialog_confirm), useUnmergedTree = true)
            .performClick()

        composeRule.onNodeWithText(text(PresentationR.string.search_entries_title)).assertIsDisplayed()
        assertNull(fakeBackend.diaryByDate(diary.date))
    }

    private fun openDiaryFromSearch(query: String, date: LocalDate) {
        composeRule
            .onNodeWithContentDescription(text(PresentationR.string.search), useUnmergedTree = true)
            .performClick()
        composeRule.onNodeWithTag("search_query_field").performTextInput(query)
        composeRule.onNodeWithTag("search_query_field").performImeAction()
        composeRule.onNodeWithTag("search_diary_card_$date").performClick()
    }
}
