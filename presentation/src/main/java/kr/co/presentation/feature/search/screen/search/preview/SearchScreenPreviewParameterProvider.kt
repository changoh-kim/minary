package kr.co.presentation.feature.search.screen.search.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.core.common.model.Emotion
import kr.co.presentation.feature.search.model.SearchDiaryUiModel
import kr.co.presentation.feature.search.screen.search.SearchScreenState
import java.time.LocalDate

class SearchScreenPreviewParameterProvider :
    PreviewParameterProvider<SearchScreenState> {

    override val values = sequenceOf(
        // 1. 초기 진입 상태 (최근 일기 노출)
        SearchScreenState(
            diaries = listOf(
                SearchDiaryUiModel(
                    title = "A beautiful afternoon",
                    content = "The sun was setting over the hills, casting a golden glow over everything.",
                    emotions = listOf(Emotion.JOY),
                    date = LocalDate.now()
                ),
                SearchDiaryUiModel(
                    title = "Reflections on the project",
                    content = "Finished the main wireframes today. It's starting to look like a real app.",
                    emotions = listOf(Emotion.CALMNESS),
                    date = LocalDate.now().minusDays(1)
                )
            )
        ),
        // 2. 최근 검색어 노출 상태
        SearchScreenState(
            recentSearches = listOf("Beautiful", "Project", "Diary", "Weather", "Food")
        ),
        // 3. 검색 중 상태
        SearchScreenState(
            searchQuery = "Search query",
            isLoading = true
        ),
        // 4. 검색 결과 없음 상태
        SearchScreenState(
            searchQuery = "Non-existent",
            diaries = emptyList(),
            isLoading = false
        ),
        // 5. 날짜 범위 필터링 상태
        SearchScreenState(
            startDate = LocalDate.now().minusWeeks(1),
            endDate = LocalDate.now(),
            diaries = listOf(
                SearchDiaryUiModel(
                    title = "Filtered Result",
                    content = "Only showing diaries from the selected range.",
                    emotions = listOf(Emotion.SATISFACTION),
                    date = LocalDate.now().minusDays(3)
                )
            )
        )
    )
}
