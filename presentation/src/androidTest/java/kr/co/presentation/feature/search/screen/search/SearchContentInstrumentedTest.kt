package kr.co.presentation.feature.search.screen.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kr.co.presentation.R
import kr.co.presentation.testing.BaseComposeTest
import kr.co.presentation.testing.PresentationComposeFixtures
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchContentInstrumentedTest : BaseComposeTest() {

    @Test
    fun emptyState_isRendered() {
        setMinaryContent {
            SearchContent(
                state = SearchScreenState(isLoading = false),
            )
        }

        composeRule.onNodeWithText(text(R.string.no_results_found)).assertIsDisplayed()
    }

    @Test
    fun loadingState_isRendered() {
        setMinaryContent {
            SearchContent(
                state = SearchScreenState(isLoading = true),
            )
        }

        composeRule.onNodeWithText(text(R.string.search_loading)).assertIsDisplayed()
    }

    @Test
    fun diaryCardClick_emitsDiaryClickedAction() {
        val diary = PresentationComposeFixtures.searchDiaryUiModel()
        val actions = mutableListOf<SearchAction>()

        setMinaryContent {
            SearchContent(
                state = SearchScreenState(diaries = listOf(diary)),
                onAction = actions::add,
            )
        }

        composeRule.onNodeWithTag("search_diary_card_${PresentationComposeFixtures.DATE}")
            .performClick()

        assertTrue(actions.contains(SearchAction.DiaryClicked(diary)))
    }
}
