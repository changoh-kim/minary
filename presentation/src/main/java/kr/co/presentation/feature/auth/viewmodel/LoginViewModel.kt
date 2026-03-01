package kr.co.presentation.feature.auth.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.domain.feature.auth.exception.AuthException
import kr.co.domain.feature.auth.usecase.LoginUseCase
import kr.co.domain.feature.diary.usecase.CheckAndDownloadInitialDiariesUseCase
import kr.co.presentation.R
import kr.co.presentation.common.extension.safeCall
import kr.co.presentation.common.model.UiText
import kr.co.presentation.feature.auth.mapper.UserUiModelMapper.toUserUiModel
import kr.co.presentation.feature.auth.model.UserUiModel
import kr.co.presentation.navigation.LoginRoute
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.SimpleSyntax
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject


@Immutable
data class LoginScreenState(
    val loggedInUser: UserUiModel? = null,
    val isLoggingIn: Boolean = false,
    val email: String = "",
    val password: String = ""
)

@Immutable
sealed interface LoginSideEffect {
    object LoginSucceeded : LoginSideEffect
    object SignUpClicked : LoginSideEffect
    data class ShowMessage(val uiText: UiText) : LoginSideEffect
}

sealed interface LoginAction {
    data class EmailChanged(val newEmail: String) : LoginAction
    data class PasswordChanged(val newPassword: String) : LoginAction
    object LoginClicked : LoginAction
    object SignUpClicked : LoginAction
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val loginUseCase: LoginUseCase,
    private val checkAndDownloadInitialDiariesUseCase: CheckAndDownloadInitialDiariesUseCase,
) : ViewModel(), ContainerHost<LoginScreenState, LoginSideEffect> {

    private companion object {
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
    }

    override val container = container<LoginScreenState, LoginSideEffect>(LoginScreenState())

    init {
        initState()
    }

    private fun initState() = intent {
        val route = savedStateHandle.toRoute<LoginRoute>()

        val savedEmail = savedStateHandle[KEY_EMAIL] ?: ""
        val savedPassword = savedStateHandle[KEY_PASSWORD] ?: ""

        reduce { state.copy(email = savedEmail, password = savedPassword) }
    }

    fun handleAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged -> updateEmail(action.newEmail)
            is LoginAction.PasswordChanged -> updatePassword(action.newPassword)
            is LoginAction.LoginClicked -> requestLogin()
            is LoginAction.SignUpClicked -> signUpClicked()
        }
    }

    private fun handleError(error: Throwable) = intent {
        val message = when (error) {
            is AuthException.SignInUserIsNullException -> UiText.StringResource(R.string.login_failed)
            else -> error.message?.let { UiText.DynamicString(it) }
                ?: UiText.StringResource(R.string.unknown_error)
        }

        postSideEffect(LoginSideEffect.ShowMessage(message))
    }

    private fun updateEmail(newEmail: String) = intent {
        reduce { state.copy(email = newEmail) }
        savedStateHandle[KEY_EMAIL] = newEmail
    }

    private fun updatePassword(newPassword: String) = intent {
        reduce { state.copy(password = newPassword) }
        savedStateHandle[KEY_PASSWORD] = newPassword
    }

    private fun requestLogin() = intent {
        if (!validateInput()) return@intent

        safeCall { loginUseCase(state.email, state.password) }
            .map { it.toUserUiModel() }
            .onLoading { isLoading -> reduce { state.copy(isLoggingIn = isLoading) } }
            .onError { handleError(it) }
            .launchOnSuccess { loggedInUser ->
                checkAndDownloadInitialDiariesUseCase()
                postSideEffect(LoginSideEffect.LoginSucceeded)
            }
    }

    private suspend fun SimpleSyntax<LoginScreenState, LoginSideEffect>.validateInput(): Boolean {
        return when {
            state.email.isBlank() -> {
                postSideEffect(LoginSideEffect.ShowMessage(UiText.StringResource(R.string.id_is_empty)))
                false
            }

            state.password.isBlank() -> {
                postSideEffect(LoginSideEffect.ShowMessage(UiText.StringResource(R.string.password_is_empty)))
                false
            }

            else -> true
        }
    }

    private fun signUpClicked() = intent { postSideEffect(LoginSideEffect.SignUpClicked) }
}