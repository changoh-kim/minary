package kr.co.presentation.feature.setting.preview

import kr.co.presentation.feature.setting.model.UserProfileUiModel
import java.time.LocalDate

internal object SettingPreviewData {
    val userProfile = UserProfileUiModel(
        uid = "uid-123",
        name = "Minary User",
        nickname = "Minary",
        email = "user@minary.com",
        phoneNumber = "010-1234-5678",
        address = "Seoul, South Korea",
        birthday = LocalDate.of(1995, 1, 1),
        profilePhotoUrl = "https://example.com/photo.jpg"
    )

    val emptyUserProfile = UserProfileUiModel()
}