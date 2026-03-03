package kr.co.presentation.feature.dashboard.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.domain.feature.dashboard.usecase.GetDashboardUseCase
import kr.co.presentation.R
import kr.co.presentation.common.extension.safeCall
import kr.co.presentation.common.model.UiText
import kr.co.presentation.common.state.LoadState
import kr.co.presentation.feature.dashboard.mapper.DashboardUiModelMapper.toDashboardUiModel
import kr.co.presentation.feature.dashboard.model.DashboardUiModel
import kr.co.presentation.navigation.DashboardRoute
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject


@Immutable
data class DashboardScreenState(
    val dashboardLoadState: LoadState<DashboardUiModel> = LoadState.Uninitialized,
)

@Immutable
sealed interface DashboardSideEffect {
    data class ShowMessage(val uiText: UiText) : DashboardSideEffect
}

sealed interface DashboardAction

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getDashboardUseCase: GetDashboardUseCase,
) : ViewModel(), ContainerHost<DashboardScreenState, DashboardSideEffect> {

    private companion object {
        private const val KEY_DASHBOARD = "dashboard"
    }

    override val container =
        container<DashboardScreenState, DashboardSideEffect>(DashboardScreenState())

    init {
        loadDashboard()
    }

    private fun loadDashboard() = intent {
        val route = savedStateHandle.toRoute<DashboardRoute>()

        val savedDashboard: DashboardUiModel? = savedStateHandle[KEY_DASHBOARD]
        if (savedDashboard != null) {
            reduce { state.copy(dashboardLoadState = LoadState.Success(savedDashboard)) }
        } else {
            safeCall { getDashboardUseCase() }
                .map { it.toDashboardUiModel() }
                .launchAsLoadState { loadState ->
                    reduce { state.copy(dashboardLoadState = loadState) }

                    when (loadState) {
                        is LoadState.Success -> savedStateHandle[KEY_DASHBOARD] = loadState.data
                        is LoadState.Error -> loadState.exception?.let { handleError(it) }
                        else -> {}
                    }
                }
        }
    }

    fun handleAction(action: DashboardAction) {}

    private fun handleError(error: Throwable) = intent {
        val message = error.message
            ?.let { UiText.DynamicString(it) }
            ?: UiText.StringResource(R.string.unknown_error)

        postSideEffect(DashboardSideEffect.ShowMessage(message))
    }
}