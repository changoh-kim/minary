package kr.co.presentation.main.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.domain.feature.diary.usecase.sync.StopRealtimeDiarySyncUseCase
import kr.co.domain.feature.profile.usecase.sync.StopRealtimeUserProfileSyncUseCase
import kr.co.domain.feature.session.usecase.GetSessionStateStreamUseCase
import kr.co.domain.feature.setting.model.AppTheme
import kr.co.domain.feature.setting.usecase.GetAppThemeStreamUseCase
import kr.co.domain.feature.setting.usecase.sync.StopRealtimeUserSettingsSyncUseCase
import kr.co.domain.feature.time.usecase.SyncServerTimeUseCase
import kr.co.domain.infra.remote.model.ServiceStatus
import kr.co.domain.infra.remote.usecase.CheckServiceStatusUseCase
import kr.co.presentation.common.model.UiText
import kr.co.presentation.common.state.LoadState
import kr.co.presentation.main.mapper.UserSessionUiModelMapper.toUserSessionUiModel
import kr.co.presentation.main.model.UserSessionUiModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@Immutable
data class MainActivityState(
    val userSession: LoadState<UserSessionUiModel?> = LoadState.Uninitialized,
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val serviceStatus: ServiceStatus = ServiceStatus.Active,
)

@Immutable
sealed interface MainActivitySideEffect {
    data class ShowMessage(val uiText: UiText) : MainActivitySideEffect
}

sealed interface MainActivityAction {
    object OnResumed : MainActivityAction
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val syncServerTime: SyncServerTimeUseCase,
    private val getAppThemeStream: GetAppThemeStreamUseCase,
    private val getSessionStateStream: GetSessionStateStreamUseCase,

    private val stopRealtimeUserProfileSync: StopRealtimeUserProfileSyncUseCase,
    private val stopRealtimeUserSettingsSync: StopRealtimeUserSettingsSyncUseCase,
    private val stopRealtimeDiarySync: StopRealtimeDiarySyncUseCase,

    private val checkServiceStatus: CheckServiceStatusUseCase,
) : ViewModel(), ContainerHost<MainActivityState, MainActivitySideEffect> {

    override val container =
        container<MainActivityState, MainActivitySideEffect>(MainActivityState())

    init {
        checkService()
        syncTime()
        collectAppTheme()
        collectUserSession()
    }

    private fun syncTime() = intent {
        syncServerTime()
    }

    private fun collectAppTheme() = intent {
        getAppThemeStream().collect { reduce { state.copy(appTheme = it) } }
    }

    private fun collectUserSession() = intent {
        getSessionStateStream().collect { userSession ->
            if (userSession != null) {
                reduce { state.copy(userSession = LoadState.Success(userSession.toUserSessionUiModel())) }
                collectAppTheme()
            } else {
                stopRealtimeSync()
                reduce { state.copy(userSession = LoadState.Success(null)) }
            }
        }
    }

    private fun stopRealtimeSync() = intent {
        stopRealtimeUserProfileSync()
        stopRealtimeUserSettingsSync()
        stopRealtimeDiarySync()
    }

    fun handleAction(action: MainActivityAction) {
        when (action) {
            is MainActivityAction.OnResumed -> checkService()
        }
    }

    private fun checkService() = intent {
        val status = checkServiceStatus()
        reduce { state.copy(serviceStatus = status) }
    }
}