package kr.co.minary.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.HiltAndroidTest
import kr.co.minary.testing.BaseE2ETest
import org.junit.Test
import java.time.LocalDate
import kr.co.presentation.R as PresentationR

@LargeTest
@HiltAndroidTest
class SearchDiaryE2ETest : BaseE2ETest() {

    @Test
    fun searchResultClick_opensDiaryDetail() {
        val diary = fakeBackend.seedDiary(
            date = LocalDate.of(2026, 1, 15),
            title = "phase2-title",
            content = "phase2-content",
        )
        fakeBackend.signInAsTestUser()

        launchApp()

        composeRule
            .onNodeWithContentDescription(text(PresentationR.string.search), useUnmergedTree = true)
            .performClick()
        composeRule.onNodeWithTag("search_query_field").performTextInput("phase2")
        composeRule.onNodeWithTag("search_query_field").performImeAction()

        composeRule.onNodeWithTag("search_diary_card_${diary.date}").performClick()

        composeRule.onNodeWithText(diary.title).assertIsDisplayed()
        composeRule.onNodeWithText(diary.content).assertIsDisplayed()
    }
}
