package kr.co.data.model.diary

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kr.co.data.local.DiaryTable.COLUMN_CONTENT
import kr.co.data.local.DiaryTable.COLUMN_DATE
import kr.co.data.local.DiaryTable.COLUMN_EMOTION_NAME
import kr.co.data.local.DiaryTable.COLUMN_ID
import kr.co.data.local.DiaryTable.COLUMN_IS_DELETED
import kr.co.data.local.DiaryTable.COLUMN_IS_SYNCED
import kr.co.data.local.DiaryTable.COLUMN_TIMESTAMP
import kr.co.data.local.DiaryTable.COLUMN_TITLE
import kr.co.data.local.DiaryTable.TABLE_NAME
import java.time.LocalDate


@Entity(
    tableName = TABLE_NAME,
    indices = [Index(value = [COLUMN_DATE], unique = true)])
data class DiaryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID) val id: Long = 0L,

    @ColumnInfo(name = COLUMN_DATE) val date: LocalDate,
    @ColumnInfo(name = COLUMN_TITLE) val title: String,
    @ColumnInfo(name = COLUMN_CONTENT) val content: String,
    @ColumnInfo(name = COLUMN_EMOTION_NAME) val emotionName: String,

    @ColumnInfo(name = COLUMN_TIMESTAMP) val timestamp: Long,
    @ColumnInfo(name = COLUMN_IS_SYNCED) val isSynced: Boolean,
    @ColumnInfo(name = COLUMN_IS_DELETED) val isDeleted: Boolean,
)