package kr.co.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = RecentSearchEntity.TABLE_NAME)
data class RecentSearchEntity(
    @PrimaryKey
    @ColumnInfo(name = COLUMN_QUERY) val query: String,
    @ColumnInfo(name = COLUMN_TIMESTAMP) val timestamp: Long
) {
    companion object {
        const val TABLE_NAME = "recent_search"
        const val COLUMN_QUERY = "search_query"
        const val COLUMN_TIMESTAMP = "timestamp"
    }
}