package kr.co.presentation.feature.account.screen.signin

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

class SignInContentInstrumentedTest : BaseComposeTest() {

    @Test
    fun inputAndClick_emitSignInActions() {
        val actions = mutableListOf<SignInAction>()

        setMinaryContent {
            SignInContent(
                email = "",
                password = "",
                onAction = actions::add,
            )
        }

        composeRule.onNodeWithTag("sign_in_email_field")
            .performTextInput(PresentationComposeFixtures.EMAIL)
        composeRule.onNodeWithTag("sign_in_password_field")
            .performTextInput("Password1")
        composeRule.onNodeWithTag("sign_in_submit_button")
            .performClick()

        assertTrue(actions.contains(SignInAction.EmailChanged(PresentationComposeFixtures.EMAIL)))
        assertTrue(actions.contains(SignInAction.PasswordChanged("Password1")))
        assertTrue(actions.contains(SignInAction.SignInClicked))
    }

    @Test
    fun signUpLink_emitsSignUpClickedAction() {
        val actions = mutableListOf<SignInAction>()

        setMinaryContent {
            SignInContent(onAction = actions::add)
        }

        composeRule.onNodeWithTag("sign_in_sign_up_link")
            .performClick()

        assertTrue(actions.contains(SignInAction.SignUpClicked))
    }

    @Test
    fun loadingState_disablesSubmitButton() {
        setMinaryContent {
            SignInContent(isSigningIn = true)
        }

        composeRule.onNodeWithText(text(R.string.btn_sign_in)).assertIsDisplayed()
        composeRule.onNodeWithTag("sign_in_submit_button").assertIsNotEnabled()
    }
}
