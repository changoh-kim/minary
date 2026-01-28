package kr.co.presentation.mapper

import kr.co.domain.model.diary.DiaryData
import kr.co.presentation.ui.model.common.UiDiary


object UiDiaryMapper {
    fun DiaryData.toUiDiary() = UiDiary(
        id = id,
        date = date,
        title = title,
        content = content,
        emotion = emotion,
    )

    fun UiDiary.toDiaryData() = DiaryData(
        id = id,
        date = date,
        title = title,
        content = content,
        emotion = emotion,
    )
}