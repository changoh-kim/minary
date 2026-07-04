package kr.co.presentation.feature.home.screen.home

import app.cash.turbine.test
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kr.co.core.common.error.DomainError
import kr.co.core.common.state.SyncProcessState
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
import kr.co.presentation.testing.BaseViewModelTest
import kr.co.presentation.testing.PresentationFixtures
import kr.co.presentation.testing.assertStringResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest : BaseViewModelTest() {
    private val initDiarySyncState = MutableStateFlow<SyncProcessState>(SyncProcessState.Idle)
    private val diarySyncEnabled = MutableStateFlow(true)
    private val getInitDiarySyncStateStream = mockk<GetInitDiarySyncStateStreamUseCase>()
    private val getCurrentUser = mockk<GetCurrentUserUseCase>()
    private val initUserStorage = mockk<InitUserStorageUseCase>()
    private val startUserDataSync = mockk<StartUserDataSyncUseCase>()
    private val startDiarySync = mockk<StartDiarySyncUseCase>()
    private val stopDiaryFullSync = mockk<StopDiaryFullSyncUseCase>(relaxed = true)
    private val stopDiaryPeriodicSync = mockk<StopDiaryPeriodicSyncUseCase>(relaxed = true)
    private val getDiarySettingsStream = mockk<GetDiarySettingsStreamUseCase>()
    private val reloadSession = mockk<ReloadSessionUseCase>()
    private val startRealtimeUserProfileSync = mockk<StartRealtimeUserProfileSyncUseCase>()
    private val startRealtimeUserSettingsSync = mockk<StartRealtimeUserSettingsSyncUseCase>()
    private val startRealtimeDiarySync = mockk<StartRealtimeDiarySyncUseCase>()
    private val stopRealtimeUserProfileSync = mockk<StopRealtimeUserProfileSyncUseCase>(relaxed = true)
    private val stopRealtimeUserSettingsSync = mockk<StopRealtimeUserSettingsSyncUseCase>(relaxed = true)
    private val stopRealtimeDiarySync = mockk<StopRealtimeDiarySyncUseCase>(relaxed = true)

    @Test
    fun `init starts user and diary sync when current user exists`() = runPresentationTest {
        stubSuccessfulStartup()

        val viewModel = createViewModel()
        viewModel.stateFlow().test {
            awaitItem()
            advanceUntilIdle()

            coVerify { initUserStorage(PresentationFixtures.UID) }
            coVerify { startUserDataSync(PresentationFixtures.UID) }
            coVerify { startRealtimeUserProfileSync() }
            coVerify { startRealtimeUserSettingsSync() }
            coVerify { startDiarySync() }
            coVerify { startRealtimeDiarySync() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init reflects init diary sync process state`() = runPresentationTest {
        stubSuccessfulStartup()
        val viewModel = createViewModel()
        advanceUntilIdle()

        initDiarySyncState.value = SyncProcessState.InProgress.Determinate(0.5f)
        advanceUntilIdle()

        assertEquals(SyncProcessState.InProgress.Determinate(0.5f), viewModel.stateFlow().value.initDiarySyncProcessState)
    }

    @Test
    fun `storage init failure marks critical error and emits message`() = runPresentationTest {
        every { getInitDiarySyncStateStream() } returns initDiarySyncState
        coEvery { getCurrentUser() } returns Ok(PresentationFixtures.userSession())
        coEvery { initUserStorage(PresentationFixtures.UID) } returns Err(DomainError.Store.PermissionDenied)

        val viewModel = createViewModel()

        assertEquals(true, viewModel.awaitState { it.isCriticalError }.isCriticalError)
        viewModel.sideEffectFlow().test {
            val sideEffect = awaitItem() as HomeSideEffect.ShowMessage
            assertStringResource(R.string.home_error_storage_init, sideEffect.uiText)
        }
        coVerify(exactly = 0) { startUserDataSync(any()) }
    }

    @Test
    fun `disabled diary sync stops scheduled and realtime diary sync`() = runPresentationTest {
        stubSuccessfulStartup(syncEnabled = false)

        val viewModel = createViewModel()
        viewModel.stateFlow().test {
            awaitItem()
            advanceUntilIdle()

            verify { stopDiaryFullSync() }
            verify { stopDiaryPeriodicSync() }
            verify { stopRealtimeDiarySync() }
            coVerify(exactly = 0) { startDiarySync() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry starts diary sync and realtime diary sync`() = runPresentationTest {
        every { getInitDiarySyncStateStream() } returns initDiarySyncState
        coEvery { getCurrentUser() } returns Err(DomainError.Auth.UserNotFound)
        coEvery { startDiarySync() } returns Ok(Unit)
        coEvery { startRealtimeDiarySync() } returns Ok(Unit)
        val viewModel = createViewModel()
        viewModel.stateFlow().test {
            awaitItem()
            advanceUntilIdle()

            viewModel.handleAction(HomeAction.RetryClicked)
            advanceUntilIdle()

            coVerify { startDiarySync() }
            coVerify { startRealtimeDiarySync() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun stubSuccessfulStartup(syncEnabled: Boolean = true) {
        every { getInitDiarySyncStateStream() } returns initDiarySyncState
        every { getDiarySettingsStream() } returns diarySyncEnabled
        diarySyncEnabled.value = syncEnabled
        coEvery { getCurrentUser() } returns Ok(PresentationFixtures.userSession())
        coEvery { initUserStorage(PresentationFixtures.UID) } returns Ok(Unit)
        coEvery { startUserDataSync(PresentationFixtures.UID) } returns Ok(Unit)
        coEvery { startRealtimeUserProfileSync() } returns Ok(Unit)
        coEvery { startRealtimeUserSettingsSync() } returns Ok(Unit)
        coEvery { startDiarySync() } returns Ok(Unit)
        coEvery { startRealtimeDiarySync() } returns Ok(Unit)
        coEvery { reloadSession() } returns Ok(PresentationFixtures.userSession())
    }

    private fun createViewModel() = HomeViewModel(
        getInitDiarySyncProcessStateStream = getInitDiarySyncStateStream,
        getCurrentUser = getCurrentUser,
        initUserStorage = initUserStorage,
        startUserDataSync = startUserDataSync,
        startDiarySync = startDiarySync,
        stopDiaryFullSync = stopDiaryFullSync,
        stopDiaryPeriodicSync = stopDiaryPeriodicSync,
        getDiarySettingsStream = getDiarySettingsStream,
        reloadSession = reloadSession,
        startRealtimeUserProfileSync = startRealtimeUserProfileSync,
        startRealtimeUserSettingsSync = startRealtimeUserSettingsSync,
        startRealtimeDiarySync = startRealtimeDiarySync,
        stopRealtimeUserProfileSync = stopRealtimeUserProfileSync,
        stopRealtimeUserSettingsSync = stopRealtimeUserSettingsSync,
        stopRealtimeDiarySync = stopRealtimeDiarySync,
    ).trackOrbitViewModel()
}
