package kr.co.presentation.feature.search.screen.search

import app.cash.turbine.test
import com.github.michaelbull.result.Ok
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.diary.usecase.GetPagedDiariesUseCase
import kr.co.domain.feature.search.usecase.AddRecentSearchUseCase
import kr.co.domain.feature.search.usecase.ClearAllRecentSearchesUseCase
import kr.co.domain.feature.search.usecase.GetRecentSearchesUseCase
import kr.co.domain.feature.search.usecase.RemoveRecentSearchUseCase
import kr.co.presentation.testing.BaseViewModelTest
import kr.co.presentation.testing.PresentationFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest : BaseViewModelTest() {
    private val getPagedDiaries = mockk<GetPagedDiariesUseCase>()
    private val getRecentSearches = mockk<GetRecentSearchesUseCase>()
    private val addRecentSearch = mockk<AddRecentSearchUseCase>(relaxed = true)
    private val removeRecentSearch = mockk<RemoveRecentSearchUseCase>(relaxed = true)
    private val clearAllRecentSearches = mockk<ClearAllRecentSearchesUseCase>(relaxed = true)
    private val recentSearches = MutableStateFlow(listOf("recent-test"))

    @Test
    fun `init loads recent searches and first diary page`() = runPresentationTest {
        every { getRecentSearches() } returns recentSearches
        coEvery { getPagedDiaries(null, null, null, 20, 0) } returns Ok(listOf(PresentationFixtures.diary()))

        val viewModel = createViewModel()

        val state = viewModel.awaitState { it.recentSearches == listOf("recent-test") && it.diaries.isNotEmpty() }
        assertEquals(listOf("recent-test"), state.recentSearches)
        assertEquals(listOf(PresentationFixtures.searchDiaryUiModel()), state.diaries)
        assertEquals(false, state.isLoading)
        assertEquals(false, state.hasNextPage)
    }

    @Test
    fun `search trims query stores recent search and replaces first page`() = runPresentationTest {
        every { getRecentSearches() } returns recentSearches
        coEvery { getPagedDiaries(null, null, null, 20, 0) } returns Ok(emptyList())
        coEvery { getPagedDiaries("query-test", null, null, 20, 0) } returns Ok(listOf(PresentationFixtures.diary(title = "searched")))
        val viewModel = createViewModel()
        viewModel.awaitState { !it.isLoading }

        viewModel.handleAction(SearchAction.Search("  query-test  "))
        val state = viewModel.awaitState { it.diaries.singleOrNull()?.title == "searched" }

        coVerify { addRecentSearch("query-test") }
        assertEquals("searched", state.diaries.single().title)
    }

    @Test
    fun `load next page appends diaries using current offset`() = runPresentationTest {
        every { getRecentSearches() } returns recentSearches
        val firstPage = diaries(count = 20, titlePrefix = "first")
        val nextPage = listOf(PresentationFixtures.diary(id = "next-id", title = "next"))
        coEvery { getPagedDiaries(null, null, null, 20, 0) } returns Ok(firstPage)
        coEvery { getPagedDiaries(null, null, null, 20, 20) } returns Ok(nextPage)
        val viewModel = createViewModel()
        viewModel.awaitState { it.diaries.size == 20 && it.hasNextPage }

        viewModel.handleAction(SearchAction.LoadNextPage)
        val state = viewModel.awaitState { it.diaries.size == 21 }

        coVerify { getPagedDiaries(null, null, null, 20, 20) }
        assertEquals("next", state.diaries.last().title)
        assertEquals(false, state.hasNextPage)
    }

    @Test
    fun `diary click emits clicked date side effect`() = runPresentationTest {
        every { getRecentSearches() } returns recentSearches
        coEvery { getPagedDiaries(null, null, null, 20, 0) } returns Ok(emptyList())
        val viewModel = createViewModel()
        viewModel.awaitState { !it.isLoading }

        viewModel.sideEffectFlow().test {
            viewModel.handleAction(SearchAction.DiaryClicked(PresentationFixtures.searchDiaryUiModel()))
            advanceUntilIdle()

            assertEquals(SearchSideEffect.DiaryClicked(PresentationFixtures.DATE), awaitItem())
        }
    }

    private fun diaries(
        count: Int,
        titlePrefix: String,
    ): List<Diary> = List(count) { index ->
        PresentationFixtures.diary(
            id = "diary-id-$index",
            title = "$titlePrefix-$index",
        )
    }

    private fun createViewModel() = SearchViewModel(
        getPagedDiaries = getPagedDiaries,
        getRecentSearches = getRecentSearches,
        addRecentSearch = addRecentSearch,
        removeRecentSearch = removeRecentSearch,
        clearAllRecentSearches = clearAllRecentSearches,
    ).trackOrbitViewModel()
}
