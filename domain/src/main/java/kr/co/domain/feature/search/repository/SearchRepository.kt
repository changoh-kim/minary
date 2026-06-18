package kr.co.domain.feature.search.repository

import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun getRecentSearches(): Flow<List<String>>
    suspend fun addRecentSearch(query: String, timestamp: Long)
    suspend fun removeRecentSearch(query: String)
    suspend fun clearAll()
}