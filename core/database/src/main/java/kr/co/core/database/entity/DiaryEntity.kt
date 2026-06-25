package kr.co.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kr.co.core.database.entity.DiaryEntity.Companion.COLUMN_DATE
import kr.co.core.database.entity.DiaryEntity.Companion.TABLE_NAME
import kr.co.core.common.state.DiarySyncStatus
import java.time.LocalDate
import java.util.UUID


@Entity(
    tableName = TABLE_NAME,
    indices = [
        Index(value = [COLUMN_DATE], unique = true),
    ]
)
data class DiaryEntity(
    @PrimaryKey
    @ColumnInfo(name = COLUMN_ID) val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = COLUMN_DATE) val date: LocalDate,
    @ColumnInfo(name = COLUMN_TITLE) val title: String,
    @ColumnInfo(name = COLUMN_CONTENT) val content: String,
    @ColumnInfo(name = COLUMN_CREATED_AT) val createdAt: Long,
    @ColumnInfo(name = COLUMN_LAST_MODIFIED_AT) val lastModifiedAt: Long,
    @ColumnInfo(name = COLUMN_SYNC_STATUS) val syncStatus: DiarySyncStatus,
) {
    companion object {
        const val TABLE_NAME = "diary"

        const val COLUMN_ID = "id"
        const val COLUMN_DATE = "date"
        const val COLUMN_TITLE = "title"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_CREATED_AT = "createdAt"
        const val COLUMN_LAST_MODIFIED_AT = "lastModifiedAt"
        const val COLUMN_SYNC_STATUS = "syncStatus"
    }
}