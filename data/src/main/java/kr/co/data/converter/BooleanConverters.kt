package kr.co.data.converter

import androidx.room.TypeConverter


object BooleanConverters {

    @TypeConverter
    fun fromBoolean(value: Boolean?): Int? {
        return if (value == null) null else if (value) 1 else 0
    }

    @TypeConverter
    fun toBoolean(value: Int?): Boolean? {
        return if (value == null) null else value == 1
    }
}