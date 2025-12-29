package kr.co.presentation.viewmodel.main

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.domain.exception.AuthException
import kr.co.domain.model.auth.User
import kr.co.domain.usecase.IsUserLoggedInUseCase
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject


sealed class MainActivityUiState {
    object Loading : MainActivityUiState()
    object Auth : MainActivityUiState()
    object Main : MainActivityUiState()
}

@Immutable
data class MainActivityState(
    val uiState: MainActivityUiState? = MainActivityUiState.Loading,
    val loginUser: User? = null
)

@Immutable
sealed class MainActivitySideEffect {
    data class ShowMsg(val msg: String) : MainActivitySideEffect() // 오류 메시지 표시
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase
) : ViewModel(), ContainerHost<MainActivityState, MainActivitySideEffect> {

    override val container = container<MainActivityState, MainActivitySideEffect>(MainActivityState())

    init {
        checkLogin()
    }

    fun checkLogin() = intent {
        val loggedIn = isUserLoggedInUseCase()
        loggedIn.onSuccess { user ->
            reduce {
                state.copy(loginUser = user, uiState = MainActivityUiState.Main)
            }
        }.onFailure { error ->
            reduce {
                state.copy(loginUser = null, uiState = MainActivityUiState.Auth)
            }

            when(error) {
                is AuthException.CurrentUserIsNullException -> {
                    postSideEffect(MainActivitySideEffect.ShowMsg("Current user is null"))
                }
                else -> {
                    postSideEffect(MainActivitySideEffect.ShowMsg(error.message.toString()))
                }
            }
        }
    }
}