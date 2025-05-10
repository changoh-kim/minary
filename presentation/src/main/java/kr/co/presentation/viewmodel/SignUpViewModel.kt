package kr.co.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kr.co.domain.exception.AuthException
import kr.co.domain.usecase.SignUpUseCase
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

sealed class SignUpSideEffect {
    object NavigateToLoginScreen : SignUpSideEffect()

    data class ShowMsg(val msg: String) : SignUpSideEffect()
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel(), ContainerHost<SignUpState, SignUpSideEffect> {

    override val container = container<SignUpState, SignUpSideEffect>(SignUpState())

    fun onEmailChanged(id: String) = blockingIntent {
        reduce { state.copy(email = id) }
    }

    fun onUserNameChanged(userName: String) = blockingIntent {
        reduce { state.copy(userName = userName) }
    }

    fun onPasswordChanged(password: String) = blockingIntent {
        reduce { state.copy(password = password) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) = blockingIntent {
        reduce { state.copy(confirmPassword = confirmPassword) }
    }

    fun signUp() = intent {
        if (state.email.isNullOrBlank()) {
            postSideEffect(SignUpSideEffect.ShowMsg("Email is empty"))
            return@intent
        }

        if (state.userName.isNullOrBlank()) {
            postSideEffect(SignUpSideEffect.ShowMsg("Name is empty"))
            return@intent
        }

        if (state.password.isNullOrBlank()) {
            postSideEffect(SignUpSideEffect.ShowMsg("Password is empty"))
            return@intent
        }

        if (state.confirmPassword.isNullOrBlank()) {
            postSideEffect(SignUpSideEffect.ShowMsg("Confirm password is empty"))
            return@intent
        }

        if (state.password != state.confirmPassword) {
            postSideEffect(SignUpSideEffect.ShowMsg("Password does not match"))
            return@intent
        }

        val signUp = signUpUseCase(state.email, state.password, state.userName)
        signUp.onSuccess { user ->
            postSideEffect(SignUpSideEffect.NavigateToLoginScreen)
        }.onFailure { error ->
            when (error) {
                is AuthException.CreateUserIsNullException -> {
                    postSideEffect(SignUpSideEffect.ShowMsg("Create account failed"))
                }

                else -> {
                    postSideEffect(SignUpSideEffect.ShowMsg(error.message.toString()))
                }
            }
        }
    }
}