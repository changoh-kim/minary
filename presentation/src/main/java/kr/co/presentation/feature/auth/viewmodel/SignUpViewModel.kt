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
import kr.co.presentation.feature.auth.mapper.UserUiModelMapper.toUserUiModel
import kr.co.presentation.feature.auth.model.UserUiModel
import kr.co.presentation.navigation.SignUpRoute
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
    val signedUpUser: UserUiModel? = null,
    val isSigningUp: Boolean = false,
    val email: String = "",
    val name: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)

@Immutable
sealed interface SignUpSideEffect {
    object SignUpSucceeded : SignUpSideEffect
    data class ShowMessage(val uiText: UiText) : SignUpSideEffect
}

sealed interface SignUpAction {
    data class EmailChanged(val newEmail: String) : SignUpAction
    data class NameChanged(val newName: String) : SignUpAction
    data class PasswordChanged(val newPassword: String) : SignUpAction
    data class ConfirmPasswordChanged(val newConfirmPassword: String) : SignUpAction
    object SignUpClicked : SignUpAction
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

        val savedEmail = savedStateHandle[KEY_EMAIL] ?: ""
        val savedName = savedStateHandle[KEY_NAME] ?: ""
        val savedPassword = savedStateHandle[KEY_PASSWORD] ?: ""
        val savedConfirmPassword = savedStateHandle[KEY_CONFIRM_PASSWORD] ?: ""

        reduce {
            state.copy(
                email = savedEmail,
                name = savedName,
                password = savedPassword,
                confirmPassword = savedConfirmPassword
            )
        }
    }

    fun handleAction(action: SignUpAction) {
        when (action) {
            is SignUpAction.EmailChanged -> updateEmail(action.newEmail)
            is SignUpAction.NameChanged -> updateName(action.newName)
            is SignUpAction.PasswordChanged -> updatePassword(action.newPassword)
            is SignUpAction.ConfirmPasswordChanged -> updateConfirmPassword(action.newConfirmPassword)
            is SignUpAction.SignUpClicked -> requestSignUp()
        }
    }

    private fun handleUpError(error: Throwable) = intent {
        val message = when (error) {
            is AuthException.CreateUserIsNullException -> UiText.StringResource(R.string.account_creation_failed)
            else -> error.message?.let { UiText.DynamicString(it) }
                ?: UiText.StringResource(R.string.unknown_error)
        }

        postSideEffect(SignUpSideEffect.ShowMessage(message))
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

    private fun requestSignUp() = intent {
        if (!validateInput()) return@intent

        safeCall { signUpUseCase(state.email, state.password, state.name) }
            .map { it.toUserUiModel() }
            .onLoading { isLoading -> reduce { state.copy(isSigningUp = isLoading) } }
            .onError { handleUpError(it) }
            .launchOnSuccess { signedUpUser ->
                postSideEffect(SignUpSideEffect.SignUpSucceeded)
            }
    }

    private suspend fun SimpleSyntax<SignUpScreenState, SignUpSideEffect>.validateInput(): Boolean {
        return when {
            state.email.isBlank() -> {
                postSideEffect(SignUpSideEffect.ShowMessage(UiText.StringResource(R.string.email_is_empty)))
                false
            }

            state.name.isBlank() -> {
                postSideEffect(SignUpSideEffect.ShowMessage(UiText.StringResource(R.string.name_is_empty)))
                false
            }

            state.password.isBlank() -> {
                postSideEffect(SignUpSideEffect.ShowMessage(UiText.StringResource(R.string.password_is_empty)))
                false
            }

            state.confirmPassword.isBlank() -> {
                postSideEffect(SignUpSideEffect.ShowMessage(UiText.StringResource(R.string.confirm_password_is_empty)))
                false
            }

            state.password != state.confirmPassword -> {
                postSideEffect(SignUpSideEffect.ShowMessage(UiText.StringResource(R.string.password_not_match)))
                false
            }

            else -> true
        }
    }
}