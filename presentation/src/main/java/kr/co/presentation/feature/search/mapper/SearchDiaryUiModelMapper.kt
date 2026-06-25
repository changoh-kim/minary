package kr.co.presentation.feature.search.mapper

import kr.co.domain.feature.diary.model.Diary
import kr.co.presentation.feature.search.model.SearchDiaryUiModel

object SearchDiaryUiModelMapper {
    fun Diary.toSearchDiaryUiModel() = SearchDiaryUiModel(
        date = date,
        title = title,
        content = content,
        emotions = emotions,
        imageUrls = imageUrls,
    )
}
