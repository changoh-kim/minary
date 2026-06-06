package kr.co.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kr.co.data.local.entity.DiaryEmotionEntity.Companion.COLUMN_DIARY_ID
import kr.co.data.local.entity.DiaryEmotionEntity.Companion.COLUMN_EMOTION
import kr.co.data.local.entity.DiaryEmotionEntity.Companion.TABLE_NAME
import kr.co.domain.feature.emotion.model.Emotion


@Entity(
    tableName = TABLE_NAME,
    primaryKeys = [COLUMN_DIARY_ID, COLUMN_EMOTION],
    foreignKeys = [
        ForeignKey(
            entity = DiaryEntity::class,
            parentColumns = [DiaryEntity.COLUMN_ID],
            childColumns = [COLUMN_DIARY_ID],
            onDelete = ForeignKey.CASCADE  // 일기 삭제 시 감정도 자동 삭제
        )
    ],
    indices = [Index(value = [COLUMN_DIARY_ID])]
)
data class DiaryEmotionEntity(
    @ColumnInfo(name = COLUMN_DIARY_ID) val diaryId: String,
    @ColumnInfo(name = COLUMN_EMOTION) val emotion: Emotion
) {
    companion object {
        const val TABLE_NAME = "diaryEmotion"

        const val COLUMN_DIARY_ID = "diaryId"
        const val COLUMN_EMOTION = "emotion"
    }
}