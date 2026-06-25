package kr.co.presentation.feature.setting.screen.profiledetail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.github.michaelbull.result.onOk
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.core.ui.common.text.UiText
import kr.co.domain.feature.profile.usecase.GetUserProfileStreamUseCase
import kr.co.presentation.feature.setting.mapper.UserProfileUiModelMapper.toUserProfileUiModel
import kr.co.presentation.feature.setting.model.UserProfileUiModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@Immutable
data class ProfileDetailScreenState(
    val userProfile: UserProfileUiModel = UserProfileUiModel(),
)

@Immutable
sealed interface ProfileDetailSideEffect {
    object EditClicked : ProfileDetailSideEffect
    object BackClicked : ProfileDetailSideEffect
    object NavigateToAccountDeletion : ProfileDetailSideEffect
    data class ShowMessage(val uiText: UiText) : ProfileDetailSideEffect
}

sealed interface ProfileDetailAction {
    object BackClicked : ProfileDetailAction
    object EditClicked : ProfileDetailAction
    object DeleteAccountClicked : ProfileDetailAction
}

@HiltViewModel
class ProfileDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getUserProfileStream: GetUserProfileStreamUseCase,
) : ViewModel(), ContainerHost<ProfileDetailScreenState, ProfileDetailSideEffect> {

    override val container =
        container<ProfileDetailScreenState, ProfileDetailSideEffect>(ProfileDetailScreenState())

    init {
        collectUserProfile()
    }

    private fun collectUserProfile() = intent {
        getUserProfileStream().collect { result ->
            result.onOk { user ->
                reduce { state.copy(userProfile = user.toUserProfileUiModel()) }
            }
        }
    }

    fun handleAction(action: ProfileDetailAction) {
        when (action) {
            is ProfileDetailAction.BackClicked -> backClicked()
            is ProfileDetailAction.EditClicked -> editClicked()
            is ProfileDetailAction.DeleteAccountClicked -> navigateToAccountDeletion()
        }
    }

    private fun backClicked() = intent {
        postSideEffect(ProfileDetailSideEffect.BackClicked)
    }

    private fun editClicked() = intent {
        postSideEffect(ProfileDetailSideEffect.EditClicked)
    }

    private fun navigateToAccountDeletion() = intent {
        postSideEffect(ProfileDetailSideEffect.NavigateToAccountDeletion)
    }
}