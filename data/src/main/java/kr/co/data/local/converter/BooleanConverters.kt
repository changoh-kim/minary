package kr.co.data.local.converter

import androidx.room.TypeConverter
import kr.co.domain.common.Converter


object BooleanConverters {

    @TypeConverter
    fun fromBoolean(value: Boolean?): Int? {
        return value?.let { Converter.toInt(it) }
    }

    @TypeConverter
    fun toBoolean(value: Int?): Boolean? {
        return value?.let { Converter.toBoolean(it) }
    }
}