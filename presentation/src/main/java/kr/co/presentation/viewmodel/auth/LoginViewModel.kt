package kr.co.presentation.viewmodel.auth

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.domain.exception.AuthException
import kr.co.domain.usecase.LoginUseCase
import kr.co.presentation.R
import kr.co.presentation.ui.model.common.UiText
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.blockingIntent
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject


@Immutable
data class LoginState(
    val isLoggingIn: Boolean = false,
    val loginError: UiText? = null,
    val id: String = "",
    val password: String = ""
)

@Immutable
sealed interface LoginSideEffect {
    object NavigateToMainScreen : LoginSideEffect
    object NavigateToSignUpScreen : LoginSideEffect
    data class ShowMsg(val uiText: UiText) : LoginSideEffect
}

sealed interface LoginIntent {
    data class IdChanged(val newId: String) : LoginIntent
    data class PasswordChanged(val newPassword: String) : LoginIntent
    object LoginButtonClicked : LoginIntent
    object SignUpButtonClicked : LoginIntent
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel(), ContainerHost<LoginState, LoginSideEffect> {

    override val container = container<LoginState, LoginSideEffect>(LoginState())

    fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.IdChanged -> updateId(intent.newId)
            is LoginIntent.PasswordChanged -> updatePassword(intent.newPassword)
            is LoginIntent.LoginButtonClicked -> login()
            is LoginIntent.SignUpButtonClicked -> navigateToSignUpScreen()
        }
    }

    private fun updateId(newId: String) = blockingIntent {
        reduce { state.copy(id = newId) }
    }

    private fun updatePassword(newPassword: String) = blockingIntent {
        reduce { state.copy(password = newPassword) }
    }

    private fun login() = intent {
        if (state.id.isNullOrBlank()) {
            postSideEffect(LoginSideEffect.ShowMsg(UiText.StringResource(R.string.id_is_empty)))
            return@intent
        }

        if (state.password.isNullOrBlank()) {
            postSideEffect(LoginSideEffect.ShowMsg(UiText.StringResource(R.string.password_is_empty)))
            return@intent
        }

        reduce {
            // 로그인 시도 중, 오류 메시지 초기화
            state.copy(
                isLoggingIn = true,
                loginError = UiText.StringResource(R.string.unknown_error)
            )
        }

        val login = loginUseCase(state.id, state.password)
        login.onSuccess {
            reduce {
                state.copy(
                    isLoggingIn = false,
                    loginError = UiText.StringResource(R.string.unknown_error)
                )
            }

            postSideEffect(LoginSideEffect.NavigateToMainScreen)
        }.onFailure { error ->
            reduce {
                // 로그인 실패, 오류 메시지 업데이트
                state.copy(
                    isLoggingIn = false,
                    loginError = UiText.StringResource(R.string.login_failed)
                )
            }

            when (error) {
                is AuthException.SignInUserIsNullException -> {
                    postSideEffect(LoginSideEffect.ShowMsg(UiText.StringResource(R.string.login_failed)))
                }

                else -> {
                    val uiText = error.message
                        .takeIf { !it.isNullOrBlank() }
                        ?.let { UiText.DynamicString(it) }
                        ?: UiText.StringResource(R.string.unknown_error)

                    postSideEffect(LoginSideEffect.ShowMsg(uiText))
                }
            }
        }
    }

    private fun navigateToSignUpScreen() = intent {
        postSideEffect(LoginSideEffect.NavigateToSignUpScreen)
    }
}