package kr.co.presentation.main.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.domain.feature.auth.exception.AuthException
import kr.co.domain.feature.auth.usecase.IsUserLoggedInUseCase
import kr.co.domain.feature.diary.usecase.SyncDiaryUseCase
import kr.co.presentation.R
import kr.co.presentation.common.model.UiText
import kr.co.presentation.feature.auth.mapper.UserUiModelMapper.toUserUiModel
import kr.co.presentation.feature.auth.model.UserUiModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject


@Immutable
sealed interface StartDestination {
    object Splash : StartDestination
    object Welcome : StartDestination
    object Main : StartDestination
}

@Immutable
data class MainActivityUiState(
    val startDestination: StartDestination = StartDestination.Splash,
    val loginUser: UserUiModel? = UserUiModel()
)

@Immutable
sealed interface MainActivitySideEffect {
    data class ShowMsg(val uiText: UiText) : MainActivitySideEffect // 오류 메시지 표시
}

sealed interface MainActivityIntent {

}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase,
    private val syncDiaryUseCase: SyncDiaryUseCase,
) : ViewModel(), ContainerHost<MainActivityUiState, MainActivitySideEffect> {

    override val container = container<MainActivityUiState, MainActivitySideEffect>(MainActivityUiState())

    init {
        checkLogin()
    }

    private fun checkLogin() = intent {
        val loggedIn = isUserLoggedInUseCase()
        loggedIn.onSuccess { user ->

            reduce {
                state.copy(loginUser = user.toUserUiModel(), startDestination = StartDestination.Main)
            }

            syncDiaryUseCase()
        }.onFailure { error ->
            reduce {
                state.copy(loginUser = null, startDestination = StartDestination.Welcome)
            }

            when(error) {
                is AuthException.CurrentUserIsNullException -> {
                    postSideEffect(MainActivitySideEffect.ShowMsg(UiText.StringResource(R.string.current_user_is_null)))
                }
                else -> {
                    val uiText = error.message
                        .takeIf { !it.isNullOrBlank() }
                        ?.let { UiText.DynamicString(it) }
                        ?: UiText.StringResource(R.string.unknown_error)

                    postSideEffect(MainActivitySideEffect.ShowMsg(uiText))
                }
            }
        }
    }
}