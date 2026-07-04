package kr.co.presentation.feature.setting.screen.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import kr.co.core.common.model.AppTheme
import kr.co.presentation.R
import kr.co.presentation.testing.BaseComposeTest
import kr.co.presentation.testing.PresentationComposeFixtures
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsContentInstrumentedTest : BaseComposeTest() {

    @Test
    fun profileAndSettings_areRendered() {
        setMinaryContent {
            SettingsContent(state = settingsState())
        }

        composeRule.onNodeWithText(PresentationComposeFixtures.NAME).assertIsDisplayed()
        composeRule.onNodeWithText(PresentationComposeFixtures.EMAIL).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.settings_theme)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.settings_diary_sync)).assertIsDisplayed()
    }

    @Test
    fun interactions_emitSettingsActions() {
        val actions = mutableListOf<SettingsAction>()

        setMinaryContent {
            SettingsContent(
                state = settingsState(),
                onAction = actions::add,
            )
        }

        composeRule.onNodeWithTag("settings_profile_card").performClick()
        composeRule.onNodeWithText(text(R.string.settings_theme_light)).performClick()
        composeRule.onNodeWithTag("settings_diary_sync_switch").performClick()
        composeRule.onNodeWithTag("settings_sign_out_button")
            .performScrollTo()
            .performClick()

        assertTrue(actions.contains(SettingsAction.UserProfileClicked))
        assertTrue(actions.contains(SettingsAction.ThemeChanged(AppTheme.LIGHT)))
        assertTrue(actions.contains(SettingsAction.DiarySyncEnabledChanged(false)))
        assertTrue(actions.contains(SettingsAction.SignOutClicked))
    }

    private fun settingsState() = SettingsScreenState(
        userProfile = PresentationComposeFixtures.userProfileUiModel(),
        userSettings = PresentationComposeFixtures.userSettingsUiModel(
            appTheme = AppTheme.DARK,
            diarySyncEnabled = true,
        ),
    )
}
