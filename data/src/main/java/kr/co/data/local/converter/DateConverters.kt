package kr.co.data.local.converter

import androidx.room.TypeConverter
import kr.co.domain.common.Converter
import java.time.LocalDate


object DateConverters {

    @TypeConverter
    fun fromString(value: String?): LocalDate? {
        return value?.let { Converter.toDate(it) }
    }

    @TypeConverter
    fun toString(date: LocalDate?): String? {
        return date?.let { Converter.toDateString(it) }
    }
}