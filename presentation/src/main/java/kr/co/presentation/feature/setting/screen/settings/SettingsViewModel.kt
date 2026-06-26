package kr.co.presentation.feature.setting.screen.settings

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.github.michaelbull.result.get
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kr.co.core.common.error.DomainError
import kr.co.core.common.extension.TAG
import kr.co.core.common.model.AppTheme
import kr.co.core.ui.common.load.load
import kr.co.core.ui.common.text.UiText
import kr.co.domain.feature.account.usecase.SignOutUseCase
import kr.co.domain.feature.diary.usecase.setting.UpdateDiarySyncEnabledUseCase
import kr.co.domain.feature.diary.usecase.sync.CheckDiarySyncStateUseCase
import kr.co.domain.feature.profile.usecase.GetUserProfileStreamUseCase
import kr.co.domain.feature.setting.usecase.GetUserSettingsStreamUseCase
import kr.co.domain.feature.setting.usecase.UpdateAppThemeUseCase
import kr.co.presentation.R
import kr.co.presentation.feature.setting.mapper.UserProfileUiModelMapper.toUserProfileUiModel
import kr.co.presentation.feature.setting.mapper.UserSettingsUiModelMapper.toUserSettingsUiModel
import kr.co.presentation.feature.setting.model.UserProfileUiModel
import kr.co.presentation.feature.setting.model.UserSettingsUiModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@Immutable
data class SettingsScreenState(
    val userProfile: UserProfileUiModel = UserProfileUiModel(),
    val userSettings: UserSettingsUiModel = UserSettingsUiModel(),
)

@Immutable
sealed interface SettingsSideEffect {
    object UserProfileClicked : SettingsSideEffect
    object SignOutSucceeded : SettingsSideEffect
    data class ShowSignOutDialog(val uiText: UiText? = null) : SettingsSideEffect
    data class ShowMessage(val uiText: UiText) : SettingsSideEffect
}

sealed interface SettingsAction {
    object UserProfileClicked : SettingsAction
    object SignOutClicked : SettingsAction
    object SignOutConfirmed : SettingsAction
    data class ThemeChanged(val newAppTheme: AppTheme) : SettingsAction
    data class DiarySyncEnabledChanged(val isEnabled: Boolean) : SettingsAction
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val signOut: SignOutUseCase,
    private val getUserProfileStream: GetUserProfileStreamUseCase,
    private val getUserSettingsStream: GetUserSettingsStreamUseCase,
    private val updateAppTheme: UpdateAppThemeUseCase,
    private val updateDiarySyncEnabled: UpdateDiarySyncEnabledUseCase,
    private val checkDiarySyncState: CheckDiarySyncStateUseCase,
) : ViewModel(), ContainerHost<SettingsScreenState, SettingsSideEffect> {

    override val container =
        container<SettingsScreenState, SettingsSideEffect>(SettingsScreenState())

    init {
        collectUserSettings()
    }

    private fun collectUserSettings() = intent {
        combine(
            getUserProfileStream(),
            getUserSettingsStream()
        ) { userProfileResult, userSettingsResult ->
            val userProfile = userProfileResult.get()?.toUserProfileUiModel()
            val userSettings = userSettingsResult.get()?.toUserSettingsUiModel()

            Pair(userProfile, userSettings)
        }.collect { (userProfile, userSettings) ->
            reduce {
                state.copy(
                    userProfile = userProfile ?: state.userProfile,
                    userSettings = userSettings ?: state.userSettings
                )
            }
        }
    }

    fun handleAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.UserProfileClicked -> userProfileClicked()
            is SettingsAction.SignOutClicked -> showSignOutDialog()
            is SettingsAction.SignOutConfirmed -> requestSignOut()
            is SettingsAction.ThemeChanged -> requestUpdateTheme(action.newAppTheme)
            is SettingsAction.DiarySyncEnabledChanged -> requestUpdateDiarySyncEnabled(action.isEnabled)
        }
    }

    private fun handleSignOutDialogError(error: DomainError) = intent {
        when (error) {
            DomainError.NetworkUnavailable,
            DomainError.Timeout -> Unit
            else -> {
                Log.e(TAG, "Failed to prepare sign out dialog: $error")
                postSideEffect(SettingsSideEffect.ShowMessage(UiText.StringResource(R.string.unexpected_error)))
            }
        }
    }

    private fun handleSignOutError(error: DomainError) = intent {
        when (error) {
            DomainError.NetworkUnavailable,
            DomainError.Timeout -> Unit
            else -> {
                Log.e(TAG, "Failed to sign out: $error")
                postSideEffect(SettingsSideEffect.ShowMessage(UiText.StringResource(R.string.unexpected_error)))
            }
        }
    }

    private fun handleThemeError(error: DomainError) = intent {
        Log.e(TAG, "Failed to set up theme: $error")
        postSideEffect(SettingsSideEffect.ShowMessage(UiText.StringResource(R.string.unexpected_error)))
    }

    private fun handleDiarySyncEnabledError(error: DomainError) = intent {
        Log.e(TAG, "Failed to set up diary synchronization: $error")
        postSideEffect(SettingsSideEffect.ShowMessage(UiText.StringResource(R.string.unexpected_error)))
    }

    private fun userProfileClicked() = intent {
        postSideEffect(SettingsSideEffect.UserProfileClicked)
    }

    private fun showSignOutDialog() = intent {
        load { checkDiarySyncState() }
            .onError { handleSignOutDialogError(it) }
            .startOnSuccess { postSideEffect(SettingsSideEffect.ShowSignOutDialog()) }
    }

    private fun requestSignOut() = intent {
        load { signOut() }
            .onError { handleSignOutError(it) }
            .startOnSuccess { postSideEffect(SettingsSideEffect.SignOutSucceeded) }
    }

    private fun requestUpdateTheme(newAppTheme: AppTheme) = intent {
        load { updateAppTheme(newAppTheme) }
            .onError { handleThemeError(it) }
            .start()
    }

    private fun requestUpdateDiarySyncEnabled(enabled: Boolean) = intent {
        load { updateDiarySyncEnabled(enabled) }
            .onError { handleDiarySyncEnabledError(it) }
            .start()
    }
}
