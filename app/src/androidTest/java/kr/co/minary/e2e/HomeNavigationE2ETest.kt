package kr.co.minary.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.HiltAndroidTest
import kr.co.minary.testing.BaseE2ETest
import org.junit.Test
import kr.co.presentation.R as PresentationR

@LargeTest
@HiltAndroidTest
class HomeNavigationE2ETest : BaseE2ETest() {

    @Test
    fun bottomNavigation_movesAcrossMainTabs() {
        fakeBackend.signInAsTestUser()

        launchApp()

        composeRule
            .onNodeWithText(text(PresentationR.string.calendar).uppercase())
            .assertIsDisplayed()

        composeRule
            .onNodeWithContentDescription(text(PresentationR.string.dashboard), useUnmergedTree = true)
            .performClick()
        composeRule.onNodeWithText(text(PresentationR.string.dashboard_total_entries)).assertIsDisplayed()

        composeRule
            .onNodeWithContentDescription(text(PresentationR.string.search), useUnmergedTree = true)
            .performClick()
        composeRule.onNodeWithText(text(PresentationR.string.search_entries_title)).assertIsDisplayed()

        composeRule
            .onNodeWithContentDescription(text(PresentationR.string.setting), useUnmergedTree = true)
            .performClick()
        composeRule.onNodeWithText(text(PresentationR.string.settings_appearance)).assertIsDisplayed()
    }

    @Test
    fun settingsSignOut_returnsToWelcome() {
        fakeBackend.signInAsTestUser()

        launchApp()

        composeRule
            .onNodeWithContentDescription(text(PresentationR.string.setting), useUnmergedTree = true)
            .performClick()
        composeRule
            .onNodeWithTag("settings_sign_out_button")
            .performScrollTo()
            .performClick()

        composeRule.onNodeWithText(text(PresentationR.string.dialog_confirm)).performClick()

        composeRule
            .onNodeWithText(text(PresentationR.string.welcome_title))
            .assertIsDisplayed()
    }
}
