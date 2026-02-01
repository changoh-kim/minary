package kr.co.presentation.feature.auth.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.domain.feature.auth.exception.AuthException
import kr.co.domain.feature.auth.usecase.SignUpUseCase
import kr.co.presentation.R
import kr.co.presentation.common.model.UiText
import kr.co.presentation.feature.auth.navigation.SignUpRoute
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.blockingIntent
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.annotation.concurrent.Immutable
import javax.inject.Inject


@Immutable
data class SignUpUiState(
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
    private val savedStateHandle: SavedStateHandle,
    private val signUpUseCase: SignUpUseCase
) : ViewModel(), ContainerHost<SignUpUiState, SignUpSideEffect> {

    companion object {
        private const val KEY_EMAIL = "email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PASSWORD = "password"
        private const val KEY_CONFIRM_PASSWORD = "confirm_password"
    }

    override val container = container<SignUpUiState, SignUpSideEffect>(SignUpUiState())

    init {
        initializeState()
    }

    private fun initializeState() = intent {
        val route = savedStateHandle.toRoute<SignUpRoute>()

        val email = savedStateHandle.get<String>(KEY_EMAIL) ?: ""
        val userName = savedStateHandle.get<String>(KEY_USER_NAME) ?: ""
        val password = savedStateHandle.get<String>(KEY_PASSWORD) ?: ""
        val confirmPassword = savedStateHandle.get<String>(KEY_CONFIRM_PASSWORD) ?: ""

        reduce {
            state.copy(
                email = email,
                userName = userName,
                password = password,
                confirmPassword = confirmPassword
            )
        }
    }

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
        savedStateHandle[KEY_EMAIL] = newEmail
    }

    private fun updateUserName(newUserName: String) = blockingIntent {
        reduce { state.copy(userName = newUserName) }
        savedStateHandle[KEY_USER_NAME] = newUserName
    }

    private fun updatePassword(newPassword: String) = blockingIntent {
        reduce { state.copy(password = newPassword) }
        savedStateHandle[KEY_PASSWORD] = newPassword
    }

    private fun updateConfirmPassword(newConfirmPassword: String) = blockingIntent {
        reduce { state.copy(confirmPassword = newConfirmPassword) }
        savedStateHandle[KEY_CONFIRM_PASSWORD] = newConfirmPassword
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