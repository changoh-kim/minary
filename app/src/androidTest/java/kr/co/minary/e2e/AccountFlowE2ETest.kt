package kr.co.minary.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.HiltAndroidTest
import kr.co.minary.testing.BaseE2ETest
import org.junit.Test
import kr.co.presentation.R as PresentationR

@LargeTest
@HiltAndroidTest
class AccountFlowE2ETest : BaseE2ETest() {

    @Test
    fun welcomeToSignIn_success_navigatesToHome() {
        launchApp()

        composeRule.onNodeWithText(text(PresentationR.string.sign_in)).performClick()
        composeRule.onNodeWithTag("sign_in_email_field").performTextInput("user@example.com")
        composeRule.onNodeWithTag("sign_in_password_field").performTextInput("password-test")
        composeRule.onNodeWithTag("sign_in_submit_button").performClick()

        composeRule
            .onNodeWithText(text(PresentationR.string.calendar).uppercase())
            .assertIsDisplayed()
    }

    @Test
    fun signInFailure_keepsUserOnSignInAndShowsError() {
        fakeBackend.setSignInFailure()

        launchApp()

        composeRule.onNodeWithText(text(PresentationR.string.sign_in)).performClick()
        composeRule.onNodeWithTag("sign_in_email_field").performTextInput("user@example.com")
        composeRule.onNodeWithTag("sign_in_password_field").performTextInput("wrong-password")
        composeRule.onNodeWithTag("sign_in_submit_button").performClick()

        composeRule
            .onNodeWithText(text(PresentationR.string.invalid_credentials))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(text(PresentationR.string.welcome_back))
            .assertIsDisplayed()
    }

    @Test
    fun signUpLink_opensCreateAccountScreen() {
        launchApp()

        composeRule.onNodeWithText(text(PresentationR.string.sign_in)).performClick()
        composeRule.onNodeWithTag("sign_in_sign_up_link").performClick()

        composeRule
            .onNodeWithText(text(PresentationR.string.create_account_title))
            .assertIsDisplayed()
    }
}
