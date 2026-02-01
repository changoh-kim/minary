package kr.co.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kr.co.data.local.converter.BooleanConverters
import kr.co.data.local.converter.DateConverters
import kr.co.data.local.converter.EmotionConverters
import kr.co.data.local.dao.DiaryDao
import kr.co.data.local.entity.DiaryEntity


@Database(
    entities = [DiaryEntity::class],
    version = 1
)
@TypeConverters(
    DateConverters::class,
    BooleanConverters::class,
    EmotionConverters::class,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
}