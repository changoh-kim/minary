package kr.co.data.feature.diary.mapper

import kr.co.data.feature.diary.model.DiaryDto
import kr.co.data.local.entity.DiaryEntity
import kr.co.domain.common.Converter
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

    fun DiaryDto.toDiaryEntity() = DiaryEntity(
        id = id,
        date = Converter.toDate(dateString),
        title = title,
        content = content,
        emotion = Converter.toEmotion(emotionName),
        timestamp = timestamp,
        isSynced = true,
        isDeleted = false,
    )

    fun DiaryEntity.toDiaryDto() = DiaryDto(
        id = id,
        dateString = Converter.toDateString(date),
        title = title,
        content = content,
        emotionName = Converter.toEmotionName(emotion),
        timestamp = timestamp,
    )
}