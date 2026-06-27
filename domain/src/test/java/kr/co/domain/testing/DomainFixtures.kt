package kr.co.domain.testing

import kr.co.core.common.model.AppTheme
import kr.co.core.common.model.Emotion
import kr.co.core.common.model.Gender
import kr.co.core.common.state.DiarySyncStatus
import kr.co.domain.feature.account.model.Account
import kr.co.domain.feature.account.model.SignUpInfo
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.profile.model.UserProfile
import kr.co.domain.feature.session.model.UserSession
import kr.co.domain.feature.setting.model.UserSettings
import java.time.LocalDate

object DomainFixtures {
    const val UID = "uid-test"
    const val OTHER_UID = "uid-other"
    const val EMAIL = "email-test"
    const val FIXED_TIME = 1_700_000_000_000L

    val DATE: LocalDate = LocalDate.of(2026, 1, 15)
    val BIRTHDAY: LocalDate = LocalDate.of(2000, 1, 1)

    fun account(
        uid: String = UID,
        email: String = EMAIL,
    ): Account = Account(
        uid = uid,
        email = email,
    )

    fun signUpInfo(
        email: String = EMAIL,
        name: String = "name-test",
    ): SignUpInfo = SignUpInfo(
        email = email,
        password = "value-test",
        name = name,
        gender = Gender.NONE,
        birthday = BIRTHDAY,
        address = "address-test",
        phoneNumber = "phone-test",
    )

    fun userSession(
        uid: String = UID,
        email: String = EMAIL,
    ): UserSession = UserSession(
        uid = uid,
        email = email,
    )

    fun diary(
        id: String = "diary-id-test",
        date: LocalDate = DATE,
        title: String = "title-test",
        content: String = "",
        createdAt: Long = FIXED_TIME,
        updatedAt: Long = FIXED_TIME,
        syncStatus: DiarySyncStatus = DiarySyncStatus.SYNCED,
    ): Diary = Diary(
        id = id,
        date = date,
        title = title,
        content = content,
        emotions = listOf(Emotion.CALMNESS),
        imageUrls = emptyList(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus,
    )

    fun userProfile(
        uid: String = UID,
        email: String = EMAIL,
        lastModifiedAt: Long = FIXED_TIME,
    ): UserProfile = UserProfile(
        uid = uid,
        email = email,
        name = "name-test",
        gender = Gender.NONE,
        birthday = BIRTHDAY,
        address = "address-test",
        phoneNumber = "phone-test",
        nickname = "nickname-test",
        profilePhotoUrl = "photo-url-test",
        joinedAt = FIXED_TIME,
        lastModifiedAt = lastModifiedAt,
    )

    fun userSettings(
        appTheme: AppTheme = AppTheme.SYSTEM,
        diarySyncEnabled: Boolean = false,
        lastModifiedAt: Long = FIXED_TIME,
    ): UserSettings = UserSettings(
        appTheme = appTheme,
        diarySyncEnabled = diarySyncEnabled,
        lastModifiedAt = lastModifiedAt,
    )
}
