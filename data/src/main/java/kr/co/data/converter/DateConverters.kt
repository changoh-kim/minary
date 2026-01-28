package kr.co.data.converter

import androidx.room.TypeConverter
import kr.co.domain.extention.dateToString
import kr.co.domain.extention.toLocalDate
import java.time.LocalDate


object DateConverters {

    @TypeConverter
    fun fromString(value: String?): LocalDate? {
        return value?.toLocalDate()
    }

    @TypeConverter
    fun dateToString(date: LocalDate?): String? {
        return date?.dateToString()
    }
}