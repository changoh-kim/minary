package kr.co.presentation.feature.account.screen.signup

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import kr.co.presentation.R
import kr.co.presentation.testing.BaseComposeTest
import kr.co.presentation.testing.PresentationComposeFixtures
import org.junit.Assert.assertTrue
import org.junit.Test

class SignUpContentInstrumentedTest : BaseComposeTest() {

    @Test
    fun emailInputAndCheck_emitActions() {
        val actions = mutableListOf<SignUpAction>()

        setMinaryContent {
            SignUpContent(onAction = actions::add)
        }

        composeRule.onNodeWithTag("sign_up_email_field")
            .performTextInput(PresentationComposeFixtures.EMAIL)
        composeRule.onNodeWithTag("sign_up_email_check_button")
            .performClick()

        assertTrue(actions.contains(SignUpAction.EmailChanged(PresentationComposeFixtures.EMAIL)))
        assertTrue(actions.contains(SignUpAction.EmailCheckClicked))
    }

    @Test
    fun emailAvailabilityAndError_areRendered() {
        setMinaryContent {
            SignUpContent(
                emailError = "email-error-test",
                isEmailAvailable = true,
            )
        }

        composeRule.onNodeWithText("email-error-test").assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.email_is_available)).assertIsDisplayed()
    }

    @Test
    fun loadingStates_disableButtons() {
        setMinaryContent {
            SignUpContent(
                isCheckingEmail = true,
                isSigningUp = true,
            )
        }

        composeRule.onNodeWithTag("sign_up_email_check_button").assertIsNotEnabled()
        composeRule.onNodeWithTag("sign_up_submit_button").assertIsNotEnabled()
    }
}
