package kr.co.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kr.co.data.local.entity.RecentSearchEntity

@Dao
abstract class RecentSearchDao {
    @Query("SELECT ${RecentSearchEntity.COLUMN_QUERY} FROM ${RecentSearchEntity.TABLE_NAME} ORDER BY ${RecentSearchEntity.COLUMN_TIMESTAMP} DESC")
    abstract fun getRecentSearches(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertRecentSearch(recentSearch: RecentSearchEntity)

    @Query("DELETE FROM ${RecentSearchEntity.TABLE_NAME} WHERE ${RecentSearchEntity.COLUMN_QUERY} = :query")
    abstract suspend fun deleteRecentSearch(query: String)

    @Query("DELETE FROM ${RecentSearchEntity.TABLE_NAME}")
    abstract suspend fun clearAll()

    @Transaction
    open suspend fun addRecentSearchWithLimit(recentSearch: RecentSearchEntity) {
        insertRecentSearch(recentSearch)
        deleteOldSearches(10)
    }

    @Query(
        """
        DELETE FROM ${RecentSearchEntity.TABLE_NAME} 
        WHERE ${RecentSearchEntity.COLUMN_QUERY} NOT IN (
            SELECT ${RecentSearchEntity.COLUMN_QUERY} 
            FROM ${RecentSearchEntity.TABLE_NAME} 
            ORDER BY ${RecentSearchEntity.COLUMN_TIMESTAMP} DESC 
            LIMIT :limit
        )
        """
    )
    protected abstract suspend fun deleteOldSearches(limit: Int)
}