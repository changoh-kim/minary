package kr.co.data.testing

import kr.co.core.common.model.AppTheme
import kr.co.core.common.model.Emotion
import kr.co.core.common.model.Gender
import kr.co.core.common.state.DiarySyncStatus
import kr.co.core.common.state.SyncStatus
import kr.co.core.database.entity.DiaryEmotionEntity
import kr.co.core.database.entity.DiaryEntity
import kr.co.core.database.entity.DiaryImageUrlEntity
import kr.co.core.database.model.DiaryWithRelations
import kr.co.core.datastore.proto.GenderProto
import kr.co.core.datastore.proto.ThemeProto
import kr.co.core.datastore.proto.UserProfileProto
import kr.co.core.datastore.proto.UserSettingsProto
import kr.co.data.feature.account.model.AccountModel
import kr.co.data.feature.calendar.model.CalendarDayModel
import kr.co.data.feature.calendar.model.CalendarMonthModel
import kr.co.data.feature.dashboard.model.DashboardModel
import kr.co.data.feature.diary.model.DiaryDto
import kr.co.data.feature.profile.model.UserProfileDto
import kr.co.data.feature.session.model.UserSessionModel
import kr.co.data.feature.setting.model.UserSettingsDto
import kr.co.data.feature.user.model.UserModel
import kr.co.domain.feature.account.model.Account
import kr.co.domain.feature.calendar.model.CalendarDay
import kr.co.domain.feature.calendar.model.CalendarMonth
import kr.co.domain.feature.dashboard.model.Dashboard
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.profile.model.UserProfile
import kr.co.domain.feature.session.model.UserSession
import kr.co.domain.feature.setting.model.UserSettings
import kr.co.domain.feature.user.model.User
import java.time.LocalDate
import java.time.YearMonth

object DataFixtures {
    const val UID = "uid-test"
    const val EMAIL = "email-value-test"
    const val DIARY_ID = "diary-id-test"
    const val TITLE = "title-test"
    const val CONTENT = "content-test"
    const val IMAGE_URL = "image-url-test"
    const val PROFILE_PHOTO_URL = "profile-photo-url-test"
    const val NAME = "name-test"
    const val NICKNAME = "nickname-test"
    const val ADDRESS = "address-test"
    const val PHONE_NUMBER = "phone-number-test"
    const val CREATED_AT = 100L
    const val UPDATED_AT = 200L
    const val JOINED_AT = 300L

    val date: LocalDate = LocalDate.of(2026, 6, 28)
    val birthday: LocalDate = LocalDate.of(2000, 1, 2)
    val yearMonth: YearMonth = YearMonth.of(2026, 6)

    val account = Account(uid = UID, email = EMAIL)
    val accountModel = AccountModel(uid = UID, email = EMAIL)

    val user = User(uid = UID, email = EMAIL)
    val userModel = UserModel(uid = UID, email = EMAIL)

    val userSession = UserSession(uid = UID, email = EMAIL)
    val userSessionModel = UserSessionModel(uid = UID, email = EMAIL)

    val diary = Diary(
        id = DIARY_ID,
        date = date,
        title = TITLE,
        content = CONTENT,
        emotions = listOf(Emotion.JOY, Emotion.CALMNESS),
        imageUrls = listOf(IMAGE_URL),
        createdAt = CREATED_AT,
        updatedAt = UPDATED_AT,
        syncStatus = DiarySyncStatus.PENDING_UPDATE,
    )

    val diaryEntity = DiaryEntity(
        id = DIARY_ID,
        date = date,
        title = TITLE,
        content = CONTENT,
        createdAt = CREATED_AT,
        lastModifiedAt = UPDATED_AT,
        syncStatus = DiarySyncStatus.PENDING_UPDATE,
    )

    val diaryWithRelations = DiaryWithRelations(
        diary = diaryEntity,
        emotions = listOf(
            DiaryEmotionEntity(diaryId = DIARY_ID, emotion = Emotion.JOY),
            DiaryEmotionEntity(diaryId = DIARY_ID, emotion = Emotion.CALMNESS),
        ),
        imageUrls = listOf(DiaryImageUrlEntity(diaryId = DIARY_ID, imageUrl = IMAGE_URL)),
    )

    val diaryDto = DiaryDto(
        id = DIARY_ID,
        date = date.toString(),
        title = TITLE,
        content = CONTENT,
        emotions = listOf(Emotion.JOY.name, Emotion.CALMNESS.name),
        imageUrls = listOf(IMAGE_URL),
        createdAt = CREATED_AT,
        lastModifiedAt = UPDATED_AT,
    )

    val calendarDay = CalendarDay(
        date = date,
        isCurrentMonth = true,
        diary = diary,
    )

    val calendarDayModel = CalendarDayModel(
        date = date,
        isCurrentMonth = true,
        diary = diary,
    )

    val calendarMonth = CalendarMonth(
        yearMonth = yearMonth,
        days = listOf(calendarDay),
        syncStatus = SyncStatus.SYNCED,
    )

    val calendarMonthModel = CalendarMonthModel(
        yearMonth = yearMonth,
        days = listOf(calendarDayModel),
        syncStatus = SyncStatus.SYNCED,
    )

    val dashboard = Dashboard(
        recentDiaries = listOf(diary, null),
        totalDiaryCount = 3,
        weeklyDiaryCount = 2,
        totalWordCount = 100,
        longestStreak = 4,
        emotionCounts = mapOf(Emotion.JOY to 2),
    )

    val dashboardModel = DashboardModel(
        recentDiaries = listOf(diaryWithRelations, null),
        totalDiaryCount = 3,
        weeklyDiaryCount = 2,
        totalWordCount = 100,
        longestStreak = 4,
        emotionCounts = mapOf(Emotion.JOY to 2),
    )

    val userProfile = UserProfile(
        uid = UID,
        email = EMAIL,
        name = NAME,
        gender = Gender.FEMALE,
        birthday = birthday,
        address = ADDRESS,
        phoneNumber = PHONE_NUMBER,
        nickname = NICKNAME,
        profilePhotoUrl = PROFILE_PHOTO_URL,
        joinedAt = JOINED_AT,
        lastModifiedAt = UPDATED_AT,
    )

    val userProfileProto: UserProfileProto = UserProfileProto.newBuilder()
        .setUid(UID)
        .setEmail(EMAIL)
        .setName(NAME)
        .setGender(GenderProto.FEMALE)
        .setBirthday(birthday.toString())
        .setAddress(ADDRESS)
        .setPhoneNumber(PHONE_NUMBER)
        .setNickname(NICKNAME)
        .setProfilePhotoUrl(PROFILE_PHOTO_URL)
        .setJoinedAt(JOINED_AT)
        .setLastModifiedAt(UPDATED_AT)
        .build()

    val userProfileDto = UserProfileDto(
        uid = UID,
        email = EMAIL,
        name = NAME,
        gender = GenderProto.FEMALE.name,
        birthday = birthday.toString(),
        address = ADDRESS,
        phoneNumber = PHONE_NUMBER,
        nickname = NICKNAME,
        profilePhotoUrl = PROFILE_PHOTO_URL,
        joinedAt = JOINED_AT,
        lastModifiedAt = UPDATED_AT,
    )

    val userSettings = UserSettings(
        appTheme = AppTheme.DARK,
        diarySyncEnabled = true,
        lastModifiedAt = UPDATED_AT,
    )

    val userSettingsProto: UserSettingsProto = UserSettingsProto.newBuilder()
        .setTheme(ThemeProto.DARK)
        .setDiarySyncEnabled(true)
        .setLastModifiedAt(UPDATED_AT)
        .build()

    val userSettingsDto = UserSettingsDto(
        theme = ThemeProto.DARK.name,
        diarySyncEnabled = true,
        lastModifiedAt = UPDATED_AT,
    )
}
