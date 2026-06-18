package kr.co.presentation.feature.search.design

import androidx.compose.runtime.Composable
import kr.co.presentation.design.MinaryPreviews
import kr.co.presentation.feature.search.screen.SearchContent
import kr.co.presentation.feature.search.viewmodel.SearchScreenState
import kr.co.presentation.theme.MinaryTheme

@MinaryPreviews
@Composable
fun SearchPreviews() {
    MinaryTheme {
        SearchContent(
            state = SearchScreenState(
                recentSearches = listOf("Beautiful day", "Project reflections")
            ),
            onAction = {}
        )
    }
}