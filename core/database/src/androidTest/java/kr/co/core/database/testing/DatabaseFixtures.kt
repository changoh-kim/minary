package kr.co.core.database.testing

import kr.co.core.common.model.Emotion
import kr.co.core.common.state.DiarySyncStatus
import kr.co.core.database.entity.DiaryEmotionEntity
import kr.co.core.database.entity.DiaryEntity
import kr.co.core.database.entity.DiaryImageUrlEntity
import kr.co.core.database.model.DiaryWithRelations
import java.time.LocalDate

object DatabaseFixtures {
    val DATE: LocalDate = LocalDate.of(2026, 6, 28)

    fun diary(
        id: String = "diary-id-test",
        date: LocalDate = DATE,
        lastModifiedAt: Long = 100L,
        syncStatus: DiarySyncStatus = DiarySyncStatus.SYNCED,
    ) = DiaryEntity(
        id = id,
        date = date,
        title = "title-test",
        content = "",
        createdAt = 1L,
        lastModifiedAt = lastModifiedAt,
        syncStatus = syncStatus,
    )

    fun relation(
        diary: DiaryEntity = diary(),
        emotions: List<Emotion> = listOf(Emotion.CALMNESS),
        imageUrls: List<String> = listOf("image-url-test"),
    ) = DiaryWithRelations(
        diary = diary,
        emotions = emotions.map { DiaryEmotionEntity(diary.id, it) },
        imageUrls = imageUrls.map { DiaryImageUrlEntity(diary.id, it) },
    )
}
