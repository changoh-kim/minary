package kr.co.presentation.testing

import kr.co.core.common.model.Emotion
import kr.co.core.common.model.AppTheme
import kr.co.core.common.model.Gender
import kr.co.core.common.state.DiarySyncStatus
import kr.co.domain.feature.account.model.Account
import kr.co.domain.feature.dashboard.model.Dashboard
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.profile.model.UserProfile
import kr.co.domain.feature.session.model.UserSession
import kr.co.domain.feature.setting.model.UserSettings
import kr.co.presentation.app.model.UserSessionUiModel
import kr.co.presentation.feature.account.model.AccountUiModel
import kr.co.presentation.feature.dashboard.model.DashboardDiaryUiModel
import kr.co.presentation.feature.dashboard.model.DashboardUiModel
import kr.co.presentation.feature.diary.model.DiaryUiModel
import kr.co.presentation.feature.search.model.SearchDiaryUiModel
import kr.co.presentation.feature.setting.model.UserProfileUiModel
import kr.co.presentation.feature.setting.model.UserSettingsUiModel
import java.time.LocalDate

object PresentationFixtures {
    const val UID = "uid-test"
    const val EMAIL = "user-test@example.test"
    const val DIARY_ID = "diary-id-test"
    const val TITLE = "title-test"
    const val CONTENT = "content-test"
    const val IMAGE_URL = "image-url-test"
    const val NAME = "name-test"
    const val ADDRESS = "address-test"
    const val PHONE_NUMBER = "01012345678"
    const val NICKNAME = "nickname-test"
    const val PROFILE_PHOTO_URL = "profile-photo-url-test"
    const val CREATED_AT = 1_000L
    const val UPDATED_AT = 2_000L
    const val JOINED_AT = 3_000L
    val DATE: LocalDate = LocalDate.of(2026, 1, 15)

    fun account(
        uid: String = UID,
        email: String = EMAIL,
    ) = Account(
        uid = uid,
        email = email,
    )

    fun accountUiModel(
        uid: String = UID,
        email: String = EMAIL,
    ) = AccountUiModel(
        uid = uid,
        email = email,
    )

    fun userSession(
        uid: String = UID,
        email: String = EMAIL,
    ) = UserSession(
        uid = uid,
        email = email,
    )

    fun userSessionUiModel(
        uid: String = UID,
        email: String = EMAIL,
    ) = UserSessionUiModel(
        uid = uid,
        email = email,
    )

    fun diary(
        id: String = DIARY_ID,
        date: LocalDate = DATE,
        title: String = TITLE,
        content: String = CONTENT,
    ) = Diary(
        id = id,
        date = date,
        title = title,
        content = content,
        emotions = listOf(Emotion.JOY),
        imageUrls = listOf(IMAGE_URL),
        createdAt = CREATED_AT,
        updatedAt = UPDATED_AT,
        syncStatus = DiarySyncStatus.SYNCED,
    )

    fun diaryUiModel(
        id: String = DIARY_ID,
        date: LocalDate = DATE,
        title: String = TITLE,
        content: String = CONTENT,
    ) = DiaryUiModel(
        id = id,
        date = date,
        title = title,
        content = content,
        emotions = listOf(Emotion.JOY),
        imageUrls = listOf(IMAGE_URL),
        createdAt = CREATED_AT,
        updatedAt = UPDATED_AT,
        timestamp = UPDATED_AT,
        syncStatus = DiarySyncStatus.SYNCED,
    )

    fun searchDiaryUiModel(
        date: LocalDate = DATE,
        title: String = TITLE,
        content: String = CONTENT,
    ) = SearchDiaryUiModel(
        date = date,
        title = title,
        content = content,
        emotions = listOf(Emotion.JOY),
        imageUrls = listOf(IMAGE_URL),
    )

    fun dashboard() = Dashboard(
        recentDiaries = listOf(diary(), null),
        totalDiaryCount = 10,
        weeklyDiaryCount = 3,
        totalWordCount = 250,
        longestStreak = 5,
        emotionCounts = mapOf(Emotion.JOY to 2),
    )

    fun dashboardUiModel() = DashboardUiModel(
        recentDiaries = listOf(
            DashboardDiaryUiModel(
                date = DATE,
                emotions = listOf(Emotion.JOY),
            ),
            null,
        ),
        totalDiaryCount = 10,
        weeklyDiaryCount = 3,
        totalWordCount = 250,
        longestStreak = 5,
        emotionCounts = mapOf(Emotion.JOY to 2),
    )

    fun userProfile(
        uid: String = UID,
        email: String = EMAIL,
        name: String = NAME,
    ) = UserProfile(
        uid = uid,
        email = email,
        name = name,
        gender = Gender.FEMALE,
        birthday = DATE,
        address = ADDRESS,
        phoneNumber = PHONE_NUMBER,
        nickname = NICKNAME,
        profilePhotoUrl = PROFILE_PHOTO_URL,
        joinedAt = JOINED_AT,
        lastModifiedAt = UPDATED_AT,
    )

    fun userProfileUiModel(
        uid: String = UID,
        email: String = EMAIL,
        name: String = NAME,
    ) = UserProfileUiModel(
        uid = uid,
        email = email,
        name = name,
        gender = Gender.FEMALE,
        birthday = DATE,
        address = ADDRESS,
        phoneNumber = PHONE_NUMBER,
        nickname = NICKNAME,
        profilePhotoUrl = PROFILE_PHOTO_URL,
        joinedAt = JOINED_AT,
        lastModifiedAt = UPDATED_AT,
    )

    fun userSettings(
        appTheme: AppTheme = AppTheme.DARK,
        diarySyncEnabled: Boolean = true,
    ) = UserSettings(
        appTheme = appTheme,
        diarySyncEnabled = diarySyncEnabled,
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
