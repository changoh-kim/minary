package kr.co.data.feature.diary.mapper

import kr.co.data.feature.diary.model.DiaryDto
import kr.co.data.local.entity.DiaryEntity
import kr.co.domain.common.extension.toEmotion
import kr.co.domain.common.extension.toLocalDate
import kr.co.domain.feature.diary.model.Diary


object DiaryDataMapper {

    fun Diary.toDiaryEntity() = DiaryEntity(
        id = id,
        date = date,
        title = title,
        content = content,
        emotion = emotion,
        timestamp = timestamp,
        isSynced = isSynced,
        isDeleted = isDeleted,
    )

    fun DiaryEntity.toDiary() = Diary(
        id = id,
        date = date,
        title = title,
        content = content,
        emotion = emotion,
        timestamp = timestamp,
        isSynced = isSynced,
        isDeleted = isDeleted,
    )

    fun DiaryDto.toDiaryEntity(): DiaryEntity {
        val convertedDate = date.toLocalDate()
        requireNotNull(convertedDate) { "Diary from server (id: $id) must have a valid date." }

        return DiaryEntity(
            id = id,
            date = convertedDate,
            title = title,
            content = content,
            emotion = emotionName.toEmotion(),
            timestamp = timestamp,
            isSynced = true,
            isDeleted = false,
        )
    }

    fun DiaryEntity.toDiaryDto() = DiaryDto(
        id = id,
        date = date.toString(),
        title = title,
        content = content,
        emotionName = emotion.name,
        timestamp = timestamp,
    )
}