package kr.co.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kr.co.data.local.converter.BooleanConverters
import kr.co.data.local.converter.EmotionConverters
import kr.co.data.local.converter.LocalDateConverters
import kr.co.data.local.converter.YearMonthConverters
import kr.co.data.local.dao.DiaryDao
import kr.co.data.local.dao.DiarySyncMetadataDao
import kr.co.data.local.dao.RecentSearchDao
import kr.co.data.local.entity.DiaryEmotionEntity
import kr.co.data.local.entity.DiaryEntity
import kr.co.data.local.entity.DiaryImageUrlEntity
import kr.co.data.local.entity.DiarySyncMetadataEntity
import kr.co.data.local.entity.RecentSearchEntity

@Database(
    entities = [
        DiaryEntity::class,
        DiaryEmotionEntity::class,
        DiaryImageUrlEntity::class,
        DiarySyncMetadataEntity::class,
        RecentSearchEntity::class
    ],
    version = 1
)
@TypeConverters(
    LocalDateConverters::class,
    BooleanConverters::class,
    EmotionConverters::class,
    YearMonthConverters::class,
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun diarySyncMetadataDao(): DiarySyncMetadataDao
    abstract fun recentSearchDao(): RecentSearchDao
}