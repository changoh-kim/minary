package kr.co.data.mapper

import kr.co.data.model.diary.DiaryEntity
import kr.co.data.model.diary.FirestoreDiaryDto
import kr.co.domain.extention.dateToString
import kr.co.domain.extention.toLocalDate
import kr.co.domain.model.diary.DiaryData
import kr.co.domain.model.emotion.Emotion


object DiaryEntityMapper {
    fun DiaryData.toDiaryEntity() = DiaryEntity(
        id = id,
        date = date,
        title = title,
        content = content,
        emotionName = emotion.name,
        timestamp = timestamp,
        isSynced = isSynced,
        isDeleted = isDeleted,
    )

    fun DiaryEntity.toDiaryData() = DiaryData(
        id = id,
        date = date,
        title = title,
        content = content,
        emotion = Emotion.fromString(emotionName),
        timestamp = timestamp,
        isSynced = isSynced,
        isDeleted = isDeleted,
    )

    fun FirestoreDiaryDto.toDiaryEntity() = DiaryEntity(
        id = id,
        date = date.toLocalDate(),
        title = title,
        content = content,
        emotionName = emotionName,
        timestamp = timestamp,
        isSynced = true,
        isDeleted = false,
    )

    fun DiaryEntity.toFirestoreDiaryDto() = FirestoreDiaryDto(
        id = id,
        date = date.dateToString(),
        title = title,
        content = content,
        emotionName = emotionName,
        timestamp = timestamp,
    )
}