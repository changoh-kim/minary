package kr.co.presentation.feature.auth.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.domain.feature.auth.exception.AuthException
import kr.co.domain.feature.auth.usecase.SignUpUseCase
import kr.co.presentation.R
import kr.co.presentation.common.extension.safeCall
import kr.co.presentation.common.model.UiText
import kr.co.presentation.common.state.LoadState
import kr.co.presentation.feature.auth.mapper.UserUiModelMapper.toUserUiModel
import kr.co.presentation.feature.auth.model.UserUiModel
import kr.co.presentation.feature.auth.navigation.SignUpRoute
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.SimpleSyntax
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.annotation.concurrent.Immutable
import javax.inject.Inject


@Immutable
data class SignUpScreenState(
    val signUpLoadState: LoadState<UserUiModel> = LoadState.Uninitialized,
    val isSigningUp: Boolean = false,
    val email: String = "",
    val name: String = "",
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
    data class NameChanged(val newName: String) : SignUpIntent
    data class PasswordChanged(val newPassword: String) : SignUpIntent
    data class ConfirmPasswordChanged(val newConfirmPassword: String) : SignUpIntent
    object SignUpButtonClicked : SignUpIntent
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val signUpUseCase: SignUpUseCase
) : ViewModel(), ContainerHost<SignUpScreenState, SignUpSideEffect> {

    private companion object {
        private const val KEY_EMAIL = "email"
        private const val KEY_NAME = "name"
        private const val KEY_PASSWORD = "password"
        private const val KEY_CONFIRM_PASSWORD = "confirm_password"
    }

    override val container = container<SignUpScreenState, SignUpSideEffect>(SignUpScreenState())

    init {
        initState()
    }

    private fun initState() = intent {
        val route = savedStateHandle.toRoute<SignUpRoute>()

        val savedStateEmail = savedStateHandle[KEY_EMAIL] ?: ""
        val savedStateName = savedStateHandle[KEY_NAME] ?: ""
        val savedStatePassword = savedStateHandle[KEY_PASSWORD] ?: ""
        val savedStateConfirmPassword = savedStateHandle[KEY_CONFIRM_PASSWORD] ?: ""

        reduce {
            state.copy(
                email = savedStateEmail,
                name = savedStateName,
                password = savedStatePassword,
                confirmPassword = savedStateConfirmPassword
            )
        }
    }

    fun handleIntent(intent: SignUpIntent) {
        when (intent) {
            is SignUpIntent.EmailChanged -> updateEmail(intent.newEmail)
            is SignUpIntent.NameChanged -> updateName(intent.newName)
            is SignUpIntent.PasswordChanged -> updatePassword(intent.newPassword)
            is SignUpIntent.ConfirmPasswordChanged -> updateConfirmPassword(intent.newConfirmPassword)
            is SignUpIntent.SignUpButtonClicked -> signUp()
        }
    }

    private fun updateEmail(newEmail: String) = intent {
        reduce { state.copy(email = newEmail) }
        savedStateHandle[KEY_EMAIL] = newEmail
    }

    private fun updateName(newName: String) = intent {
        reduce { state.copy(name = newName) }
        savedStateHandle[KEY_NAME] = newName
    }

    private fun updatePassword(newPassword: String) = intent {
        reduce { state.copy(password = newPassword) }
        savedStateHandle[KEY_PASSWORD] = newPassword
    }

    private fun updateConfirmPassword(newConfirmPassword: String) = intent {
        reduce { state.copy(confirmPassword = newConfirmPassword) }
        savedStateHandle[KEY_CONFIRM_PASSWORD] = newConfirmPassword
    }

    private fun signUp() = intent {
        if (!validateInput()) return@intent

        safeCall { signUpUseCase(state.email, state.password, state.name) }
            .map { it.toUserUiModel() }
            .onLoading { isLoading -> reduce { state.copy(isSigningUp = isLoading) } }
            .onError { handleSignUpError(it) }
            .launchOnSuccess { postSideEffect(SignUpSideEffect.NavigateToLoginScreen) }
    }

    private suspend fun SimpleSyntax<SignUpScreenState, SignUpSideEffect>.validateInput(): Boolean {
        return when {
            state.email.isBlank() -> {
                postSideEffect(SignUpSideEffect.ShowMsg(UiText.StringResource(R.string.email_is_empty)))
                false
            }

            state.name.isBlank() -> {
                postSideEffect(SignUpSideEffect.ShowMsg(UiText.StringResource(R.string.name_is_empty)))
                false
            }

            state.password.isBlank() -> {
                postSideEffect(SignUpSideEffect.ShowMsg(UiText.StringResource(R.string.password_is_empty)))
                false
            }

            state.confirmPassword.isBlank() -> {
                postSideEffect(SignUpSideEffect.ShowMsg(UiText.StringResource(R.string.confirm_password_is_empty)))
                false
            }

            state.password != state.confirmPassword -> {
                postSideEffect(SignUpSideEffect.ShowMsg(UiText.StringResource(R.string.password_not_match)))
                false
            }

            else -> true
        }
    }

    private fun handleSignUpError(error: Throwable) = intent {
        val message = when (error) {
            is AuthException.CreateUserIsNullException -> UiText.StringResource(R.string.account_creation_failed)
            else -> error.message?.let { UiText.DynamicString(it) }
                ?: UiText.StringResource(R.string.unknown_error)
        }
        postSideEffect(SignUpSideEffect.ShowMsg(message))
    }
}