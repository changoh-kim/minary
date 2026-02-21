package kr.co.presentation.main.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.domain.feature.auth.exception.AuthException
import kr.co.domain.feature.auth.usecase.IsUserLoggedInUseCase
import kr.co.domain.feature.diary.usecase.SyncDiaryUseCase
import kr.co.presentation.R
import kr.co.presentation.common.extension.safeCall
import kr.co.presentation.common.model.UiText
import kr.co.presentation.common.state.LoadState
import kr.co.presentation.feature.auth.mapper.UserUiModelMapper.toUserUiModel
import kr.co.presentation.feature.auth.model.UserUiModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject


@Immutable
data class MainActivityState(
    val loginLoadState: LoadState<UserUiModel> = LoadState.Uninitialized,
)

@Immutable
sealed interface MainActivitySideEffect {
    data class ShowMsg(val uiText: UiText) : MainActivitySideEffect
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase,
    private val syncDiaryUseCase: SyncDiaryUseCase,
) : ViewModel(), ContainerHost<MainActivityState, MainActivitySideEffect> {

    override val container =
        container<MainActivityState, MainActivitySideEffect>(MainActivityState())

    init {
        checkLogin()
    }

    private fun checkLogin() = intent {
        safeCall { isUserLoggedInUseCase() }
            .map { it.toUserUiModel() }
            .launchAsLoadState { loadState ->
                reduce { state.copy(loginLoadState = loadState) }

                when (loadState) {
                    is LoadState.Success -> syncDiaryUseCase()
                    is LoadState.Error -> loadState.exception?.let { handleLoginError(it) }
                    else -> {}
                }
            }
    }

    private fun handleLoginError(error: Throwable) = intent {
        val message = when (error) {
            is AuthException.CurrentUserIsNullException -> UiText.StringResource(R.string.current_user_is_null)
            else -> {
                error.message
                    ?.let { UiText.DynamicString(it) }
                    ?: UiText.StringResource(R.string.unknown_error)
            }
        }
        postSideEffect(MainActivitySideEffect.ShowMsg(message))
    }
}