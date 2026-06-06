package kr.co.data.local.converter

import androidx.room.TypeConverter
import java.time.YearMonth

object YearMonthConverters {
    @TypeConverter
    fun fromString(value: String?): YearMonth? {
        return value?.let { YearMonth.parse(it) }
    }

    @TypeConverter
    fun toString(yearMonth: YearMonth?): String? {
        return yearMonth?.toString()
    }
}