package kr.co.presentation.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.domain.usecase.LoginUseCase
import kr.co.domain.usecase.SetTokenUseCase
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

sealed class LoginSideEffect {
    object NavigateToMainScreen : LoginSideEffect() // 로그인 성공 후 메인 화면으로 이동
    data class ShowMsg(val msg: String) : LoginSideEffect() // 오류 메시지 표시
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val setTokenUseCase: SetTokenUseCase
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

    fun login() = intent {
        reduce {
            state.copy(isLoggingIn = true, loginError = "Unknown error") // 로그인 시도 중, 오류 메시지 초기화
        }

        try {
//            val token: String = loginUseCase(state.id, state.password)
            val token: String = "testToken"
            if (token.isNotBlank()) {
                // setTokenUseCase(token)
                postSideEffect(LoginSideEffect.NavigateToMainScreen) // 로그인 성공, 메인 화면으로 이동
            } else {
                reduce {
                    state.copy(
                        isLoggingIn = false,
                        loginError = "Invalid credentials"
                    ) // 로그인 실패, 오류 메시지 업데이트
                }
                postSideEffect(LoginSideEffect.ShowMsg("Invalid credentials")) // 오류 메시지 표시
            }
        } catch (e: Exception) {
            reduce {
                state.copy(isLoggingIn = false, loginError = "Login failed") // 로그인 실패, 오류 메시지 업데이트
            }
            postSideEffect(LoginSideEffect.ShowMsg("Login failed")) // 오류 메시지 표시
        }
    }
}