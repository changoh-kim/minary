package kr.co.data.testing

import kr.co.core.common.model.Emotion
import kr.co.core.common.state.DiarySyncStatus
import kr.co.core.database.entity.DiaryEmotionEntity
import kr.co.core.database.entity.DiaryEntity
import kr.co.core.database.entity.DiaryImageUrlEntity
import kr.co.core.database.model.DiaryWithRelations
import kr.co.core.datastore.proto.GenderProto
import kr.co.core.datastore.proto.ThemeProto
import kr.co.core.datastore.proto.UserProfileProto
import kr.co.core.datastore.proto.UserSettingsProto
import java.time.LocalDate

object AndroidDataFixtures {
    const val UID = "uid-test"
    const val DIARY_ID = "diary-id-test"
    const val TITLE = "title-test"
    const val CONTENT = "content-test"
    const val IMAGE_URL = "image-url-test"
    const val PROFILE_PHOTO_URL = "profile-photo-url-test"
    const val CREATED_AT = 100L
    const val UPDATED_AT = 200L

    val date: LocalDate = LocalDate.of(2026, 6, 28)
    val birthday: LocalDate = LocalDate.of(2000, 1, 2)

    val userSettingsProto: UserSettingsProto = UserSettingsProto.newBuilder()
        .setTheme(ThemeProto.DARK)
        .setDiarySyncEnabled(true)
        .setLastModifiedAt(UPDATED_AT)
        .build()

    val userProfileProto: UserProfileProto = UserProfileProto.newBuilder()
        .setUid(UID)
        .setEmail("email-value-test")
        .setName("name-test")
        .setGender(GenderProto.FEMALE)
        .setBirthday(birthday.toString())
        .setAddress("address-test")
        .setPhoneNumber("phone-number-test")
        .setNickname("nickname-test")
        .setProfilePhotoUrl(PROFILE_PHOTO_URL)
        .setJoinedAt(CREATED_AT)
        .setLastModifiedAt(UPDATED_AT)
        .build()

    fun diaryEntry(
        id: String = DIARY_ID,
        date: LocalDate = AndroidDataFixtures.date,
        title: String = TITLE,
        content: String = CONTENT,
        createdAt: Long = CREATED_AT,
        lastModifiedAt: Long = UPDATED_AT,
        syncStatus: DiarySyncStatus = DiarySyncStatus.PENDING_UPDATE,
        emotions: List<Emotion> = listOf(Emotion.JOY, Emotion.CALMNESS),
        imageUrls: List<String> = listOf(IMAGE_URL),
    ): DiaryWithRelations =
        DiaryWithRelations(
            diary = DiaryEntity(
                id = id,
                date = date,
                title = title,
                content = content,
                createdAt = createdAt,
                lastModifiedAt = lastModifiedAt,
                syncStatus = syncStatus,
            ),
            emotions = emotions.map { emotion ->
                DiaryEmotionEntity(diaryId = id, emotion = emotion)
            },
            imageUrls = imageUrls.map { imageUrl ->
                DiaryImageUrlEntity(diaryId = id, imageUrl = imageUrl)
            },
        )
}
