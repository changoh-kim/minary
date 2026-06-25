package kr.co.core.database.converter

import androidx.room.TypeConverter
import kr.co.core.common.extension.toLocalDate
import java.time.LocalDate


object LocalDateConverters {

    @TypeConverter
    fun fromLong(value: Long?): LocalDate? {
        return value?.toLocalDate()
    }

    @TypeConverter
    fun toLong(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }
}