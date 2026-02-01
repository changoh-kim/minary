package kr.co.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kr.co.data.local.table.DiaryTable
import kr.co.domain.feature.emotion.Emotion
import java.time.LocalDate


@Entity(
    tableName = DiaryTable.TABLE_NAME,
    indices = [Index(value = [DiaryTable.COLUMN_DATE], unique = true)])
data class DiaryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DiaryTable.COLUMN_ID) val id: Long = 0L,

    @ColumnInfo(name = DiaryTable.COLUMN_DATE) val date: LocalDate,
    @ColumnInfo(name = DiaryTable.COLUMN_TITLE) val title: String,
    @ColumnInfo(name = DiaryTable.COLUMN_CONTENT) val content: String,
    @ColumnInfo(name = DiaryTable.COLUMN_EMOTION) val emotion: Emotion,

    @ColumnInfo(name = DiaryTable.COLUMN_TIMESTAMP) val timestamp: Long,
    @ColumnInfo(name = DiaryTable.COLUMN_IS_SYNCED) val isSynced: Boolean,
    @ColumnInfo(name = DiaryTable.COLUMN_IS_DELETED) val isDeleted: Boolean,
)