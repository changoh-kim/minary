package kr.co.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kr.co.data.local.entity.DiaryImageUrlEntity.Companion.COLUMN_DIARY_ID
import kr.co.data.local.entity.DiaryImageUrlEntity.Companion.COLUMN_IMAGE_URL
import kr.co.data.local.entity.DiaryImageUrlEntity.Companion.TABLE_NAME

@Entity(
    tableName = TABLE_NAME,
    primaryKeys = [COLUMN_DIARY_ID, COLUMN_IMAGE_URL],
    foreignKeys = [
        ForeignKey(
            entity = DiaryEntity::class,
            parentColumns = [DiaryEntity.COLUMN_ID],
            childColumns = [COLUMN_DIARY_ID],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = [COLUMN_DIARY_ID])]
)
data class DiaryImageUrlEntity(
    @ColumnInfo(name = COLUMN_DIARY_ID) val diaryId: String,
    @ColumnInfo(name = COLUMN_IMAGE_URL) val imageUrl: String,
) {
    companion object {
        const val TABLE_NAME = "diaryImageUrl"

        const val COLUMN_DIARY_ID = "diaryId"
        const val COLUMN_IMAGE_URL = "imageUrl"
    }
}