package kr.co.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
    val id: String = "",
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

    fun onIdChanged(id: String) = blockingIntent {
        reduce { state.copy(id = id) }
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

    fun signUp() = intent{
        viewModelScope.launch {
            val isSuccessful = signUpUseCase(state.id, state.userName, state.password)
            if (isSuccessful)
                postSideEffect(SignUpSideEffect.NavigateToLoginScreen)
            else
                postSideEffect(SignUpSideEffect.ShowMsg("Unknown error"))
        }
    }
}