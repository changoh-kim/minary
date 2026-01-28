package kr.co.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kr.co.data.converter.BooleanConverters
import kr.co.data.converter.DateConverters
import kr.co.data.local.dao.DiaryDAO
import kr.co.data.model.diary.DiaryEntity


@Database(entities = [DiaryEntity::class], version = 1)
@TypeConverters(DateConverters::class, BooleanConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDAO
}