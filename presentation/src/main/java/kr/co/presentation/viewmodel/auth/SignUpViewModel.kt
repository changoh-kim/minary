package kr.co.presentation.viewmodel.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.domain.exception.AuthException
import kr.co.domain.usecase.auth.SignUpUseCase
import kr.co.presentation.R
import kr.co.presentation.ui.model.common.UiText
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.blockingIntent
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.annotation.concurrent.Immutable
import javax.inject.Inject


@Immutable
data class SignUpState(
    val email: String = "",
    val userName: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)

@Immutable
sealed interface SignUpSideEffect {
    object NavigateToLoginScreen : SignUpSideEffect
    data class ShowMsg(val uiText: UiText) : SignUpSideEffect
}

sealed interface SignUpIntent {
    data class EmailChanged(val newEmail: String) : SignUpIntent
    data class UserNameChanged(val newUserName: String) : SignUpIntent
    data class PasswordChanged(val newPassword: String) : SignUpIntent
    data class ConfirmPasswordChanged(val newConfirmPassword: String) : SignUpIntent
    object SignUpButtonClicked : SignUpIntent
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel(), ContainerHost<SignUpState, SignUpSideEffect> {

    override val container = container<SignUpState, SignUpSideEffect>(SignUpState())

    fun handelIntent(intent: SignUpIntent) {
        when (intent) {
            is SignUpIntent.EmailChanged -> updateEmail(intent.newEmail)
            is SignUpIntent.UserNameChanged -> updateUserName(intent.newUserName)
            is SignUpIntent.PasswordChanged -> updatePassword(intent.newPassword)
            is SignUpIntent.ConfirmPasswordChanged -> updateConfirmPassword(intent.newConfirmPassword)
            is SignUpIntent.SignUpButtonClicked -> signUp()
        }
    }

    private fun updateEmail(newEmail: String) = blockingIntent {
        reduce { state.copy(email = newEmail) }
    }

    private fun updateUserName(newUserName: String) = blockingIntent {
        reduce { state.copy(userName = newUserName) }
    }

    private fun updatePassword(newPassword: String) = blockingIntent {
        reduce { state.copy(password = newPassword) }
    }

    private fun updateConfirmPassword(newConfirmPassword: String) = blockingIntent {
        reduce { state.copy(confirmPassword = newConfirmPassword) }
    }

    private fun signUp() = intent {
        if (state.email.isNullOrBlank()) {
            postSideEffect(SignUpSideEffect.ShowMsg(UiText.StringResource(R.string.email_is_empty)))
            return@intent
        }

        if (state.userName.isNullOrBlank()) {
            postSideEffect(SignUpSideEffect.ShowMsg(UiText.StringResource(R.string.name_is_empty)))
            return@intent
        }

        if (state.password.isNullOrBlank()) {
            postSideEffect(SignUpSideEffect.ShowMsg(UiText.StringResource(R.string.password_is_empty)))
            return@intent
        }

        if (state.confirmPassword.isNullOrBlank()) {
            postSideEffect(SignUpSideEffect.ShowMsg(UiText.StringResource(R.string.confirm_password_is_empty)))
            return@intent
        }

        if (state.password != state.confirmPassword) {
            postSideEffect(SignUpSideEffect.ShowMsg(UiText.StringResource(R.string.password_not_match)))
            return@intent
        }

        val signUp = signUpUseCase(state.email, state.password, state.userName)
        signUp.onSuccess { user ->
            postSideEffect(SignUpSideEffect.NavigateToLoginScreen)
        }.onFailure { error ->
            when (error) {
                is AuthException.CreateUserIsNullException -> {
                    postSideEffect(SignUpSideEffect.ShowMsg(UiText.StringResource(R.string.account_creation_failed)))
                }

                else -> {
                    val uiText = error.message
                        .takeIf {
                            !it.isNullOrBlank()
                        }?.let {
                            UiText.DynamicString(it)
                        } ?: UiText.StringResource(R.string.unknown_error)

                    postSideEffect(SignUpSideEffect.ShowMsg(uiText))
                }
            }
        }
    }
}