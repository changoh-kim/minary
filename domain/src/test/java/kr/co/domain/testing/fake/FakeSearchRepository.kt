package kr.co.domain.testing.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kr.co.domain.feature.search.repository.SearchRepository

class FakeSearchRepository(
    initialSearches: List<String> = emptyList(),
) : SearchRepository {
    private val recentSearches = MutableStateFlow(initialSearches)

    val addedSearches = mutableListOf<Pair<String, Long>>()
    val removedSearches = mutableListOf<String>()
    var clearAllCallCount = 0

    var addFailure: Throwable? = null
    var removeFailure: Throwable? = null
    var clearFailure: Throwable? = null

    override fun getRecentSearches(): Flow<List<String>> = recentSearches

    override suspend fun addRecentSearch(query: String, timestamp: Long) {
        addFailure?.let { throw it }
        addedSearches += query to timestamp
        recentSearches.value = listOf(query) + recentSearches.value.filterNot { it == query }
    }

    override suspend fun removeRecentSearch(query: String) {
        removeFailure?.let { throw it }
        removedSearches += query
        recentSearches.value = recentSearches.value.filterNot { it == query }
    }

    override suspend fun clearAll() {
        clearFailure?.let { throw it }
        clearAllCallCount += 1
        recentSearches.value = emptyList()
    }
}
