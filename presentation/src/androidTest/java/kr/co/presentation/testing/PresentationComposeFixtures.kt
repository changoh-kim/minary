package kr.co.presentation.testing

import kr.co.core.common.model.AppTheme
import kr.co.core.common.model.Emotion
import kr.co.core.common.model.Gender
import kr.co.core.common.state.DiarySyncStatus
import kr.co.presentation.feature.diary.model.DiaryUiModel
import kr.co.presentation.feature.search.model.SearchDiaryUiModel
import kr.co.presentation.feature.setting.model.UserProfileUiModel
import kr.co.presentation.feature.setting.model.UserSettingsUiModel
import java.time.LocalDate

object PresentationComposeFixtures {
    const val EMAIL = "user-test@example.test"
    const val TITLE = "title-test"
    const val CONTENT = "content-test"
    const val NAME = "name-test"
    const val PROFILE_PHOTO_URL = "profile-photo-url-test"
    const val UPDATED_AT = 2_000L
    val DATE: LocalDate = LocalDate.of(2026, 1, 15)

    fun diaryUiModel(
        title: String = TITLE,
        content: String = CONTENT,
    ) = DiaryUiModel(
        id = "diary-id-test",
        date = DATE,
        title = title,
        content = content,
        emotions = listOf(Emotion.JOY),
        imageUrls = emptyList(),
        createdAt = 1_000L,
        updatedAt = UPDATED_AT,
        timestamp = UPDATED_AT,
        syncStatus = DiarySyncStatus.SYNCED,
    )

    fun searchDiaryUiModel(
        title: String = TITLE,
        content: String = CONTENT,
    ) = SearchDiaryUiModel(
        date = DATE,
        title = title,
        content = content,
        emotions = listOf(Emotion.JOY),
        imageUrls = emptyList(),
    )

    fun userProfileUiModel() = UserProfileUiModel(
        uid = "uid-test",
        email = EMAIL,
        name = NAME,
        gender = Gender.FEMALE,
        birthday = DATE,
        address = "address-test",
        phoneNumber = "01012345678",
        nickname = "nickname-test",
        profilePhotoUrl = PROFILE_PHOTO_URL,
        joinedAt = 3_000L,
        lastModifiedAt = UPDATED_AT,
    )

    fun userSettingsUiModel(
        appTheme: AppTheme = AppTheme.DARK,
        diarySyncEnabled: Boolean = true,
    ) = UserSettingsUiModel(
        appTheme = appTheme,
        isDiarySyncEnabled = diarySyncEnabled,
        lastModifiedAt = UPDATED_AT,
    )
}
