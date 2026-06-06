package kr.co.presentation.feature.setting.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.domain.error.DomainError
import kr.co.domain.feature.profile.model.Gender
import kr.co.domain.feature.profile.usecase.GetUserProfileUseCase
import kr.co.domain.feature.profile.usecase.UpdateUserProfilePhotoUseCase
import kr.co.domain.feature.profile.usecase.UpdateUserProfileUseCase
import kr.co.presentation.R
import kr.co.presentation.common.extension.TAG
import kr.co.presentation.common.extension.handleDomainError
import kr.co.presentation.common.extension.safeCall
import kr.co.presentation.common.model.UiText
import kr.co.presentation.feature.setting.mapper.UserProfileMapper.toUserProfile
import kr.co.presentation.feature.setting.mapper.UserProfileMapper.toUserProfileUiModel
import kr.co.presentation.feature.setting.model.UserProfileUiModel
import kr.co.presentation.navigation.ProfileEditRoute
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.syntax.simple.blockingIntent
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import javax.inject.Inject

@Immutable
data class ProfileEditScreenState(
    val userProfile: UserProfileUiModel = UserProfileUiModel(),
    val isPhotoLoading: Boolean = false,
)

@Immutable
sealed interface ProfileEditSideEffect {
    object BackClicked : ProfileEditSideEffect
    object PhotoEditClicked : ProfileEditSideEffect
    object ProfileUpdateSucceeded : ProfileEditSideEffect
    data class ShowMessage(val uiText: UiText) : ProfileEditSideEffect
}

sealed interface ProfileEditAction {
    data class EmailChanged(val newEmail: String) : ProfileEditAction
    data class NameChanged(val newName: String) : ProfileEditAction
    data class GenderChanged(val newGender: Gender) : ProfileEditAction
    data class BirthdayChanged(val newBirthday: LocalDate) : ProfileEditAction
    data class AddressChanged(val newAddress: String) : ProfileEditAction
    data class PhoneNumberChanged(val newPhoneNumber: String) : ProfileEditAction
    data class NicknameChanged(val newNickname: String) : ProfileEditAction
    data class ProfilePhotoPicked(val newProfilePhotoUri: Uri) : ProfileEditAction
    object BackClicked : ProfileEditAction
    object PhotoEditClicked : ProfileEditAction
    object SaveClicked : ProfileEditAction
}

@OptIn(OrbitExperimental::class)
@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getUserProfile: GetUserProfileUseCase,
    private val updateUserProfilePhoto: UpdateUserProfilePhotoUseCase,
    private val updateUserProfile: UpdateUserProfileUseCase,
) : ViewModel(), ContainerHost<ProfileEditScreenState, ProfileEditSideEffect> {

    private companion object {
        private const val KEY_USER_PROFILE = "user_profile"
    }

    override val container =
        container<ProfileEditScreenState, ProfileEditSideEffect>(ProfileEditScreenState())

    init {
        initState()
    }

    private fun initState() = intent {
        val route = savedStateHandle.toRoute<ProfileEditRoute>()
        val savedUserProfile: UserProfileUiModel? = savedStateHandle[KEY_USER_PROFILE]
        if (savedUserProfile != null) {
            reduce { state.copy(userProfile = savedUserProfile) }
        } else {
            initUserProfile()
        }
    }

    private fun initUserProfile() = intent {
        getUserProfile().onOk { userProfile ->
            updateProfile(userProfile.toUserProfileUiModel())
        }
    }

    fun handleAction(action: ProfileEditAction) {
        when (action) {
            is ProfileEditAction.EmailChanged -> updateEmail(action.newEmail)
            is ProfileEditAction.NameChanged -> updateName(action.newName)
            is ProfileEditAction.GenderChanged -> updateGender(action.newGender)
            is ProfileEditAction.BirthdayChanged -> updateBirthday(action.newBirthday)
            is ProfileEditAction.AddressChanged -> updateAddress(action.newAddress)
            is ProfileEditAction.PhoneNumberChanged -> updatePhoneNumber(action.newPhoneNumber)
            is ProfileEditAction.NicknameChanged -> updateNickname(action.newNickname)
            is ProfileEditAction.ProfilePhotoPicked -> profilePhotoPicked(action.newProfilePhotoUri)
            is ProfileEditAction.BackClicked -> backClicked()
            is ProfileEditAction.PhotoEditClicked -> photoEditClicked()
            is ProfileEditAction.SaveClicked -> requestSaveUserProfile()
        }
    }

    private fun handleUpdateProfileImageError(error: DomainError) = intent {
        handleDomainError(error) {
            networkUnavailable =
                { postSideEffect(ProfileEditSideEffect.ShowMessage(UiText.StringResource(R.string.network_unavailable))) }
            timeout =
                { postSideEffect(ProfileEditSideEffect.ShowMessage(UiText.StringResource(R.string.timeout))) }
            invalidCredentials =
                { postSideEffect(ProfileEditSideEffect.ShowMessage(UiText.StringResource(R.string.invalid_credentials))) }
            emailAlreadyInUse =
                { postSideEffect(ProfileEditSideEffect.ShowMessage(UiText.StringResource(R.string.email_already_in_use))) }
            weakPassword =
                { postSideEffect(ProfileEditSideEffect.ShowMessage(UiText.StringResource(R.string.weak_password))) }
            tooManyRequests =
                { postSideEffect(ProfileEditSideEffect.ShowMessage(UiText.StringResource(R.string.too_many_requests))) }
            unexpected = { systemError ->
                Log.e(
                    TAG,
                    "Failed to save user profile: An unexpected error has occurred",
                    systemError
                )
                if (systemError != null) {
                    postSideEffect(ProfileEditSideEffect.ShowMessage(UiText.StringResource(R.string.unexpected_error)))
                }
            }
        }
    }

    private fun handleSaveUserProfileError(error: DomainError) = intent {
        handleDomainError(error) {
            networkUnavailable =
                { postSideEffect(ProfileEditSideEffect.ShowMessage(UiText.StringResource(R.string.network_unavailable))) }
            timeout =
                { postSideEffect(ProfileEditSideEffect.ShowMessage(UiText.StringResource(R.string.timeout))) }
            invalidCredentials =
                { postSideEffect(ProfileEditSideEffect.ShowMessage(UiText.StringResource(R.string.invalid_credentials))) }
            emailAlreadyInUse =
                { postSideEffect(ProfileEditSideEffect.ShowMessage(UiText.StringResource(R.string.email_already_in_use))) }
            weakPassword =
                { postSideEffect(ProfileEditSideEffect.ShowMessage(UiText.StringResource(R.string.weak_password))) }
            tooManyRequests =
                { postSideEffect(ProfileEditSideEffect.ShowMessage(UiText.StringResource(R.string.too_many_requests))) }
            unexpected = { systemError ->
                Log.e(
                    TAG,
                    "Failed to save user profile: An unexpected error has occurred",
                    systemError
                )
                if (systemError != null) {
                    postSideEffect(ProfileEditSideEffect.ShowMessage(UiText.StringResource(R.string.unexpected_error)))
                }
            }
        }
    }

    private fun updateEmail(newEmail: String) = blockingIntent {
        updateProfile(state.userProfile.copy(email = newEmail))
    }

    private fun updateName(newName: String) = blockingIntent {
        updateProfile(state.userProfile.copy(name = newName))
    }

    private fun updateGender(newGender: Gender) = intent {
        updateProfile(state.userProfile.copy(gender = newGender))
    }

    private fun updateBirthday(newBirthday: LocalDate) = intent {
        updateProfile(state.userProfile.copy(birthday = newBirthday))
    }

    private fun updateAddress(newAddress: String) = blockingIntent {
        updateProfile(state.userProfile.copy(address = newAddress))
    }

    private fun updatePhoneNumber(newPhoneNumber: String) = blockingIntent {
        updateProfile(state.userProfile.copy(phoneNumber = newPhoneNumber))
    }

    private fun updateNickname(newNickname: String) = blockingIntent {
        updateProfile(state.userProfile.copy(nickname = newNickname))
    }

    private fun profilePhotoPicked(newProfilePhotoUri: Uri) = intent {
        safeCall<String, DomainError> { updateUserProfilePhoto(newProfilePhotoUri.toString()) }
            .onLoading { reduce { state.copy(isPhotoLoading = it) } }
            .onError { handleUpdateProfileImageError(it) }
            .launchOnSuccess { updatedProfilePhotoUrl ->
                updateProfile(state.userProfile.copy(profilePhotoUrl = updatedProfilePhotoUrl))
            }
    }

    private fun updateProfile(newUserProfile: UserProfileUiModel) = intent {
        reduce { state.copy(userProfile = newUserProfile) }
        savedStateHandle[KEY_USER_PROFILE] = newUserProfile
    }

    private fun backClicked() = intent {
        postSideEffect(ProfileEditSideEffect.BackClicked)
    }

    private fun photoEditClicked() = intent {
        postSideEffect(ProfileEditSideEffect.PhotoEditClicked)
    }

    private fun requestSaveUserProfile() = intent {
        updateUserProfile(state.userProfile.toUserProfile())
            .onErr { handleSaveUserProfileError(it) }
            .onOk { postSideEffect(ProfileEditSideEffect.ProfileUpdateSucceeded) }
    }
}