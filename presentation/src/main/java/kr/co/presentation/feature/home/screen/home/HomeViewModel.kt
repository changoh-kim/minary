package kr.co.presentation.feature.home.screen.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.github.michaelbull.result.fold
import com.github.michaelbull.result.get
import com.github.michaelbull.result.onErr
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.core.common.error.DomainError
import kr.co.core.common.state.SyncProcessState
import kr.co.core.ui.common.text.UiText
import kr.co.domain.feature.diary.usecase.setting.GetDiarySettingsStreamUseCase
import kr.co.domain.feature.diary.usecase.sync.GetInitDiarySyncStateStreamUseCase
import kr.co.domain.feature.diary.usecase.sync.StartDiarySyncUseCase
import kr.co.domain.feature.diary.usecase.sync.StartRealtimeDiarySyncUseCase
import kr.co.domain.feature.diary.usecase.sync.StopDiaryFullSyncUseCase
import kr.co.domain.feature.diary.usecase.sync.StopDiaryPeriodicSyncUseCase
import kr.co.domain.feature.diary.usecase.sync.StopRealtimeDiarySyncUseCase
import kr.co.domain.feature.profile.usecase.sync.StartRealtimeUserProfileSyncUseCase
import kr.co.domain.feature.profile.usecase.sync.StopRealtimeUserProfileSyncUseCase
import kr.co.domain.feature.session.usecase.GetCurrentUserUseCase
import kr.co.domain.feature.session.usecase.ReloadSessionUseCase
import kr.co.domain.feature.setting.usecase.sync.StartRealtimeUserSettingsSyncUseCase
import kr.co.domain.feature.setting.usecase.sync.StopRealtimeUserSettingsSyncUseCase
import kr.co.domain.feature.user.usecase.InitUserStorageUseCase
import kr.co.domain.feature.user.usecase.StartUserDataSyncUseCase
import kr.co.presentation.R
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@Immutable
data class HomeScreenState(
    val isCriticalError: Boolean = false,
    val initDiarySyncProcessState: SyncProcessState = SyncProcessState.Idle,
    val pendingSyncCount: Int = 0,
)

@Immutable
sealed interface HomeSideEffect {
    data class ShowMessage(val uiText: UiText) : HomeSideEffect
}

sealed interface HomeAction {
    object RetryClicked : HomeAction
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getInitDiarySyncProcessStateStream: GetInitDiarySyncStateStreamUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,

    private val initUserStorage: InitUserStorageUseCase,
    private val startUserDataSync: StartUserDataSyncUseCase,
    private val startDiarySync: StartDiarySyncUseCase,
    private val stopDiaryFullSync: StopDiaryFullSyncUseCase,
    private val stopDiaryPeriodicSync: StopDiaryPeriodicSyncUseCase,

    private val getDiarySettingsStream: GetDiarySettingsStreamUseCase,
    private val reloadSession: ReloadSessionUseCase,

    private val startRealtimeUserProfileSync: StartRealtimeUserProfileSyncUseCase,
    private val startRealtimeUserSettingsSync: StartRealtimeUserSettingsSyncUseCase,
    private val startRealtimeDiarySync: StartRealtimeDiarySyncUseCase,

    private val stopRealtimeUserProfileSync: StopRealtimeUserProfileSyncUseCase,
    private val stopRealtimeUserSettingsSync: StopRealtimeUserSettingsSyncUseCase,
    private val stopRealtimeDiarySync: StopRealtimeDiarySyncUseCase,
) : ViewModel(), ContainerHost<HomeScreenState, HomeSideEffect> {

    override val container =
        container<HomeScreenState, HomeSideEffect>(HomeScreenState())

    init {
        collectInitDiarySyncProcessState()
        setupUser()
    }

    private fun collectInitDiarySyncProcessState() = intent {
        getInitDiarySyncProcessStateStream().collect { state ->
            reduce { this.state.copy(initDiarySyncProcessState = state) }
        }
    }

    private fun setupUser() = intent {
        val user = getCurrentUser().get() ?: return@intent

        initUserStorage(user.uid).onErr {
            reduce { state.copy(isCriticalError = true) }
            postSideEffect(
                HomeSideEffect.ShowMessage(
                    UiText.StringResource(R.string.home_error_storage_init)
                )
            )
            return@intent
        }

        startUserDataSync(user.uid)

        startRealtimeUserProfileSync()
        startRealtimeUserSettingsSync()

        collectDiarySyncEnabled()

        reloadSession().onErr {
            postSideEffect(HomeSideEffect.ShowMessage(UiText.StringResource(R.string.home_error_session_verification)))
        }
    }

    private fun collectDiarySyncEnabled() = intent {
        getDiarySettingsStream().collect { diarySyncEnabled ->
            if (diarySyncEnabled) {
                startDiarySync().fold(
                    success = { startRealtimeDiarySync() },
                ) { handleHomeError(it) }
            } else {
                stopDiaryFullSync()
                stopDiaryPeriodicSync()
                stopRealtimeDiarySync()
            }
        }
    }

    fun handleAction(action: HomeAction) {
        when (action) {
            is HomeAction.RetryClicked -> retryInitDiarySync()
        }
    }

    private fun handleHomeError(error: DomainError) = intent {
        postSideEffect(HomeSideEffect.ShowMessage(UiText.StringResource(R.string.unknown_error)))
    }

    private fun retryInitDiarySync() = intent {
        startDiarySync().fold(
            success = { startRealtimeDiarySync() },
        ) { handleHomeError(it) }
    }

    override fun onCleared() {
        super.onCleared()
        stopRealtimeUserProfileSync()
        stopRealtimeUserSettingsSync()
        stopRealtimeDiarySync()
    }
}
