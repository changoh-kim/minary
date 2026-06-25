package kr.co.core.database.model

import androidx.room.Embedded
import androidx.room.Relation
import kr.co.core.database.entity.DiaryEmotionEntity
import kr.co.core.database.entity.DiaryEntity
import kr.co.core.database.entity.DiaryImageUrlEntity

data class DiaryWithRelations(
    @Embedded val diary: DiaryEntity,

    @Relation(
        parentColumn = DiaryEntity.Companion.COLUMN_ID,
        entityColumn = DiaryEmotionEntity.Companion.COLUMN_DIARY_ID
    )
    val emotions: List<DiaryEmotionEntity>,

    @Relation(
        parentColumn = DiaryEntity.Companion.COLUMN_ID,
        entityColumn = DiaryImageUrlEntity.Companion.COLUMN_DIARY_ID
    )
    val imageUrls: List<DiaryImageUrlEntity>
)