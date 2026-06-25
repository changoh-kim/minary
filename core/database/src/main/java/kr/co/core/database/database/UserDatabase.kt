package kr.co.core.database.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kr.co.core.database.converter.BooleanConverters
import kr.co.core.database.converter.EmotionConverters
import kr.co.core.database.converter.LocalDateConverters
import kr.co.core.database.converter.YearMonthConverters
import kr.co.core.database.dao.DiaryDao
import kr.co.core.database.dao.DiarySyncMetadataDao
import kr.co.core.database.dao.RecentSearchDao
import kr.co.core.database.entity.DiaryEmotionEntity
import kr.co.core.database.entity.DiaryEntity
import kr.co.core.database.entity.DiaryImageUrlEntity
import kr.co.core.database.entity.DiarySyncMetadataEntity
import kr.co.core.database.entity.RecentSearchEntity

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