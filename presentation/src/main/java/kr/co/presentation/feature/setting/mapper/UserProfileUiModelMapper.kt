package kr.co.presentation.feature.setting.mapper

import kr.co.domain.feature.profile.model.UserProfile
import kr.co.presentation.feature.setting.model.UserProfileUiModel

object UserProfileUiModelMapper {

    fun UserProfile.toUserProfileUiModel(): UserProfileUiModel {
        return UserProfileUiModel(
            uid = uid,
            email = email,
            name = name,
            gender = gender,
            birthday = birthday,
            address = address,
            phoneNumber = phoneNumber,
            nickname = nickname,
            profilePhotoUrl = profilePhotoUrl,
            joinedAt = joinedAt,
            lastModifiedAt = lastModifiedAt
        )
    }

    fun UserProfileUiModel.toUserProfile(): UserProfile {
        return UserProfile(
            uid = uid,
            email = email,
            name = name,
            gender = gender,
            birthday = birthday,
            address = address,
            phoneNumber = phoneNumber,
            nickname = nickname,
            profilePhotoUrl = profilePhotoUrl,
            joinedAt = joinedAt,
            lastModifiedAt = lastModifiedAt
        )
    }
}