package kr.co.data.feature.diary.mapper

import kr.co.data.feature.diary.model.DiaryDto
import kr.co.core.database.model.DiaryWithRelations
import kr.co.core.database.entity.DiaryEmotionEntity
import kr.co.core.database.entity.DiaryEntity
import kr.co.core.database.entity.DiaryImageUrlEntity
import kr.co.core.common.extension.toEmotion
import kr.co.core.common.extension.toLocalDate
import kr.co.domain.feature.diary.model.Diary
import kr.co.core.common.state.DiarySyncStatus

object DiaryMapper {
    fun Diary.toDiaryWithRelations() = DiaryWithRelations(
        diary = DiaryEntity(
            id = id,
            date = date,
            title = title,
            content = content,
            createdAt = createdAt,
            lastModifiedAt = updatedAt,
            syncStatus = syncStatus,
        ),
        emotions = emotions.map { DiaryEmotionEntity(diaryId = id, emotion = it) },
        imageUrls = imageUrls.map { DiaryImageUrlEntity(diaryId = id, imageUrl = it) },
    )

    fun DiaryWithRelations.toDiary() = Diary(
        id = diary.id,
        date = diary.date,
        title = diary.title,
        content = diary.content,
        emotions = emotions.map { it.emotion },
        imageUrls = imageUrls.map { it.imageUrl },
        createdAt = diary.createdAt,
        updatedAt = diary.lastModifiedAt,
        syncStatus = diary.syncStatus,
    )

    fun DiaryDto.toDiaryWithRelations(
        syncStatus: DiarySyncStatus,
    ): DiaryWithRelations {
        val convertedDate = date.toLocalDate()
        requireNotNull(convertedDate) { "Diary from server (id: $id) must have a valid date." }

        return DiaryWithRelations(
            diary = DiaryEntity(
                id = id,
                date = convertedDate,
                title = title,
                content = content,
                createdAt = createdAt,
                lastModifiedAt = lastModifiedAt,
                syncStatus = syncStatus,
            ),
            emotions = emotions.map { DiaryEmotionEntity(diaryId = id, emotion = it.toEmotion()) },
            imageUrls = imageUrls.map { DiaryImageUrlEntity(diaryId = id, imageUrl = it) },
        )
    }

    fun DiaryWithRelations.toDiaryDto() = DiaryDto(
        id = diary.id,
        date = diary.date.toString(),
        title = diary.title,
        content = diary.content,
        emotions = emotions.map { it.emotion.name },
        imageUrls = imageUrls.map { it.imageUrl },
        createdAt = diary.createdAt,
        lastModifiedAt = diary.lastModifiedAt,
    )
}