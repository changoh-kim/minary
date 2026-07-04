package kr.co.presentation.feature.setting.screen.settings

import app.cash.turbine.test
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kr.co.core.common.error.DomainError
import kr.co.core.common.logging.AppLogger
import kr.co.core.common.model.AppTheme
import kr.co.domain.feature.account.usecase.SignOutUseCase
import kr.co.domain.feature.diary.usecase.setting.UpdateDiarySyncEnabledUseCase
import kr.co.domain.feature.diary.usecase.sync.CheckDiarySyncStateUseCase
import kr.co.domain.feature.profile.usecase.GetUserProfileStreamUseCase
import kr.co.domain.feature.setting.usecase.GetUserSettingsStreamUseCase
import kr.co.domain.feature.setting.usecase.UpdateAppThemeUseCase
import kr.co.presentation.R
import kr.co.presentation.testing.BaseViewModelTest
import kr.co.presentation.testing.PresentationFixtures
import kr.co.presentation.testing.assertStringResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest : BaseViewModelTest() {
    private val logger = mockk<AppLogger>(relaxed = true)
    private val signOut = mockk<SignOutUseCase>()
    private val getUserProfileStream = mockk<GetUserProfileStreamUseCase>()
    private val getUserSettingsStream = mockk<GetUserSettingsStreamUseCase>()
    private val updateAppTheme = mockk<UpdateAppThemeUseCase>()
    private val updateDiarySyncEnabled = mockk<UpdateDiarySyncEnabledUseCase>()
    private val checkDiarySyncState = mockk<CheckDiarySyncStateUseCase>()
    private val profileStream = MutableStateFlow(Ok(PresentationFixtures.userProfile()))
    private val settingsStream = MutableStateFlow(Ok(PresentationFixtures.userSettings()))

    @Test
    fun `init combines profile and settings streams into ui state`() = runPresentationTest {
        stubStreams()

        val viewModel = createViewModel()

        val initialState = viewModel.awaitState { it.userProfile.uid == PresentationFixtures.UID }
        assertEquals(PresentationFixtures.userProfileUiModel(), initialState.userProfile)
        assertEquals(PresentationFixtures.userSettingsUiModel(), initialState.userSettings)

        profileStream.value = Ok(PresentationFixtures.userProfile(name = "updated-name"))
        settingsStream.value = Ok(PresentationFixtures.userSettings(appTheme = AppTheme.LIGHT, diarySyncEnabled = false))

        val updatedState = viewModel.awaitState { it.userProfile.name == "updated-name" && it.userSettings.appTheme == AppTheme.LIGHT }
        assertEquals(false, updatedState.userSettings.isDiarySyncEnabled)
    }

    @Test
    fun `sign out click checks sync state and emits dialog side effect`() = runPresentationTest {
        stubStreams()
        coEvery { checkDiarySyncState() } returns Ok(false)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.sideEffectFlow().test {
            viewModel.handleAction(SettingsAction.SignOutClicked)
            advanceUntilIdle()

            assertEquals(SettingsSideEffect.ShowSignOutDialog(), awaitItem())
        }
    }

    @Test
    fun `sign out confirmed emits succeeded side effect`() = runPresentationTest {
        stubStreams()
        coEvery { signOut() } returns Ok(Unit)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.sideEffectFlow().test {
            viewModel.handleAction(SettingsAction.SignOutConfirmed)
            advanceUntilIdle()

            assertEquals(SettingsSideEffect.SignOutSucceeded, awaitItem())
        }
    }

    @Test
    fun `theme change delegates to update app theme usecase`() = runPresentationTest {
        stubStreams()
        coEvery { updateAppTheme(AppTheme.DARK) } returns Ok(Unit)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.handleAction(SettingsAction.ThemeChanged(AppTheme.DARK))
        advanceUntilIdle()

        coVerify { updateAppTheme(AppTheme.DARK) }
    }

    @Test
    fun `diary sync change failure emits unexpected error message`() = runPresentationTest {
        stubStreams()
        coEvery { updateDiarySyncEnabled(false) } returns Err(DomainError.Store.PermissionDenied)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.sideEffectFlow().test {
            viewModel.handleAction(SettingsAction.DiarySyncEnabledChanged(false))
            advanceUntilIdle()

            val sideEffect = awaitItem() as SettingsSideEffect.ShowMessage
            assertStringResource(R.string.unexpected_error, sideEffect.uiText)
        }
    }

    private fun stubStreams() {
        coEvery { getUserProfileStream() } returns profileStream
        coEvery { getUserSettingsStream() } returns settingsStream
    }

    private fun createViewModel() = SettingsViewModel(
        logger = logger,
        signOut = signOut,
        getUserProfileStream = getUserProfileStream,
        getUserSettingsStream = getUserSettingsStream,
        updateAppTheme = updateAppTheme,
        updateDiarySyncEnabled = updateDiarySyncEnabled,
        checkDiarySyncState = checkDiarySyncState,
    ).trackOrbitViewModel()
}
