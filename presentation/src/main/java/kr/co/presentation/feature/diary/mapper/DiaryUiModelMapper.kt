package kr.co.presentation.feature.diary.mapper

import kr.co.domain.feature.diary.model.Diary
import kr.co.presentation.feature.diary.model.DiaryUiModel


object DiaryUiModelMapper {

    fun Diary.toDiaryUiModel() = DiaryUiModel(
        id = id,
        date = date,
        title = title,
        content = content,
        emotions = emotions,
        imageUrls = imageUrls,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus,
    )

    fun DiaryUiModel.toDiary() = Diary(
        id = id,
        date = date,
        title = title,
        content = content,
        emotions = emotions,
        imageUrls = imageUrls,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus,
    )
}