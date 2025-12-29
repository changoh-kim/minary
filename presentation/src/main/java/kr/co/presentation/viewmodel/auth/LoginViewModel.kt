package kr.co.presentation.viewmodel.auth

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.domain.exception.AuthException
import kr.co.domain.usecase.LoginUseCase
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
    val loginError: String = "Unknown error",
    val id: String = "",
    val password: String = ""
)

@Immutable
sealed class LoginSideEffect {
    object NavigateToMainScreen : LoginSideEffect()
    object NavigateToSignupScreen : LoginSideEffect()
    data class ShowMsg(val msg: String) : LoginSideEffect() // 오류 메시지 표시
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel(), ContainerHost<LoginState, LoginSideEffect> {

    override val container = container<LoginState, LoginSideEffect>(LoginState())

    fun onIdChanged(newValue: String) = blockingIntent {
        reduce {
            state.copy(id = newValue)
        }
    }

    fun onPasswordChanged(newValue: String) = blockingIntent {
        reduce {
            state.copy(password = newValue)
        }
    }

    fun onNavigateToSignupScreen() = intent {
        postSideEffect(LoginSideEffect.NavigateToSignupScreen)
    }

    fun login() = intent {
        if (state.id.isNullOrBlank()) {
            postSideEffect(LoginSideEffect.ShowMsg("Id is empty"))
            return@intent
        }

        if (state.password.isNullOrBlank()) {
            postSideEffect(LoginSideEffect.ShowMsg("Password is empty"))
            return@intent
        }

        reduce {
            // 로그인 시도 중, 오류 메시지 초기화
            state.copy(isLoggingIn = true, loginError = "Unknown error")
        }

        val login = loginUseCase(state.id, state.password)
        login.onSuccess {
            reduce {
                state.copy(isLoggingIn = false, loginError = "Unknown error")
            }

            postSideEffect(LoginSideEffect.NavigateToMainScreen)
        }.onFailure { error ->
            reduce {
                // 로그인 실패, 오류 메시지 업데이트
                state.copy(isLoggingIn = false, loginError = "Login failed")
            }

            when (error) {
                is AuthException.SignInUserIsNullException -> {
                    postSideEffect(LoginSideEffect.ShowMsg("Login failed"))
                }

                else -> {
                    postSideEffect(LoginSideEffect.ShowMsg(error.message.toString()))
                }
            }
        }
    }
}