package kr.co.presentation.feature.search.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.github.michaelbull.result.onOk
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kr.co.domain.feature.diary.usecase.GetPagedDiariesUseCase
import kr.co.domain.feature.search.usecase.AddRecentSearchUseCase
import kr.co.domain.feature.search.usecase.ClearAllRecentSearchesUseCase
import kr.co.domain.feature.search.usecase.GetRecentSearchesUseCase
import kr.co.domain.feature.search.usecase.RemoveRecentSearchUseCase
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiaryUiModel
import kr.co.presentation.feature.diary.model.DiaryUiModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import javax.inject.Inject

@Immutable
data class SearchScreenState(
    val searchQuery: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val recentSearches: List<String> = emptyList(),
    val diaries: List<DiaryUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isPaging: Boolean = false,
    val hasNextPage: Boolean = true,
)

sealed interface SearchSideEffect {
    data class DiaryClicked(val date: LocalDate) : SearchSideEffect
}

sealed interface SearchAction {
    data class UpdateSearchQuery(val query: String) : SearchAction
    data class Search(val query: String) : SearchAction
    data class RemoveRecentSearch(val query: String) : SearchAction
    data object ClearAllRecentSearches : SearchAction
    data class UpdateDateRange(val start: LocalDate?, val end: LocalDate?) : SearchAction
    data class DiaryClicked(val diary: DiaryUiModel) : SearchAction
    data object LoadNextPage : SearchAction
}

@OptIn(OrbitExperimental::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getPagedDiaries: GetPagedDiariesUseCase,
    private val getRecentSearches: GetRecentSearchesUseCase,
    private val addRecentSearch: AddRecentSearchUseCase,
    private val removeRecentSearch: RemoveRecentSearchUseCase,
    private val clearAllRecentSearches: ClearAllRecentSearchesUseCase,
) : ViewModel(), ContainerHost<SearchScreenState, SearchSideEffect> {

    override val container = container<SearchScreenState, SearchSideEffect>(SearchScreenState())

    companion object {
        private const val PAGE_SIZE = 20
    }

    init {
        collectRecentSearches()
        initLoadDiaries()
    }

    private fun collectRecentSearches() = intent {
        getRecentSearches().collectLatest { recentList ->
            reduce { state.copy(recentSearches = recentList) }
        }
    }

    private fun initLoadDiaries() = intent {
        reduce {
            state.copy(
                isLoading = true,
                diaries = emptyList(),
                hasNextPage = true
            )
        }

        getPagedDiaries(
            query = null,
            startDate = null,
            endDate = null,
            limit = PAGE_SIZE,
            offset = 0
        ).onOk { list ->
            val uiModels = list.map { it.toDiaryUiModel() }
            reduce {
                state.copy(
                    diaries = uiModels,
                    isLoading = false,
                    hasNextPage = list.size >= PAGE_SIZE
                )
            }
        }
    }

    fun handleAction(action: SearchAction) {
        when (action) {
            is SearchAction.UpdateSearchQuery -> intent {
                reduce { state.copy(searchQuery = action.query) }
            }

            is SearchAction.Search -> intent {
                val query = action.query.trim()
                if (query.isNotBlank()) {
                    addRecentSearch(query)
                }
                search(query, state.startDate, state.endDate)
            }

            is SearchAction.RemoveRecentSearch -> intent {
                removeRecentSearch(action.query)
            }

            is SearchAction.ClearAllRecentSearches -> intent {
                clearAllRecentSearches()
            }

            is SearchAction.UpdateDateRange -> intent {
                reduce { state.copy(startDate = action.start, endDate = action.end) }
                search(state.searchQuery, action.start, action.end)
            }

            is SearchAction.DiaryClicked -> intent {
                postSideEffect(SearchSideEffect.DiaryClicked(action.diary.date))
            }

            is SearchAction.LoadNextPage -> intent {
                if (state.isPaging || !state.hasNextPage || state.isLoading) return@intent
                loadNextDiaries()
            }
        }
    }

    private fun search(query: String?, start: LocalDate?, end: LocalDate?) = intent {
        reduce {
            state.copy(
                isLoading = true,
                diaries = emptyList(),
                hasNextPage = true
            )
        }

        getPagedDiaries(
            query = query,
            startDate = start,
            endDate = end,
            limit = PAGE_SIZE,
            offset = 0
        ).onOk { list ->
            val uiModels = list.map { it.toDiaryUiModel() }
            reduce {
                state.copy(
                    diaries = uiModels,
                    isLoading = false,
                    hasNextPage = list.size >= PAGE_SIZE
                )
            }
        }
    }

    private fun loadNextDiaries() = intent {
        reduce { state.copy(isPaging = true) }

        getPagedDiaries(
            query = state.searchQuery.takeIf { it.isNotBlank() },
            startDate = state.startDate,
            endDate = state.endDate,
            limit = PAGE_SIZE,
            offset = state.diaries.size
        ).onOk { list ->
            val uiModels = list.map { it.toDiaryUiModel() }
            reduce {
                state.copy(
                    diaries = state.diaries + uiModels,
                    isPaging = false,
                    hasNextPage = list.size >= PAGE_SIZE
                )
            }
        }
    }
}