package kr.co.data.feature.search.repository

import kotlinx.coroutines.flow.Flow
import kr.co.data.feature.search.source.SearchLocalDataSource
import kr.co.core.database.entity.RecentSearchEntity
import kr.co.domain.feature.search.repository.SearchRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val localDataSource: SearchLocalDataSource,
) : SearchRepository {
    override fun getRecentSearches(): Flow<List<String>> = localDataSource.getRecentSearches()

    override suspend fun addRecentSearch(query: String, timestamp: Long) {
        localDataSource.addRecentSearch(
            RecentSearchEntity(
                query = query,
                timestamp = timestamp
            )
        )
    }

    override suspend fun removeRecentSearch(query: String) {
        localDataSource.removeRecentSearch(query)
    }

    override suspend fun clearAll() {
        localDataSource.clearAll()
    }
}