package kr.co.data.local.converter

import androidx.room.TypeConverter
import kr.co.domain.common.extension.toLocalDate
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