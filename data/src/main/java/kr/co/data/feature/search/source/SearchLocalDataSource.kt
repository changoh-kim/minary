package kr.co.data.feature.search.source

import kotlinx.coroutines.flow.Flow
import kr.co.data.local.entity.RecentSearchEntity
import kr.co.data.local.provider.UserDatabaseProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchLocalDataSource @Inject constructor(
    private val userDatabaseProvider: UserDatabaseProvider,
) {
    private val recentSearchDao get() = userDatabaseProvider.getDatabase().recentSearchDao()

    fun getRecentSearches(): Flow<List<String>> {
        return recentSearchDao.getRecentSearches()
    }

    suspend fun addRecentSearch(entity: RecentSearchEntity) {
        recentSearchDao.addRecentSearchWithLimit(entity)
    }

    suspend fun removeRecentSearch(query: String) {
        recentSearchDao.deleteRecentSearch(query)
    }

    suspend fun clearAll() {
        recentSearchDao.clearAll()
    }
}