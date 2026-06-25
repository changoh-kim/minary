package kr.co.core.database.converter

import androidx.room.TypeConverter
import kr.co.core.common.extension.toBoolean
import kr.co.core.common.extension.toInt


object BooleanConverters {

    @TypeConverter
    fun fromBoolean(value: Boolean?): Int? {
        return value?.toInt()
    }

    @TypeConverter
    fun toBoolean(value: Int?): Boolean? {
        return value?.toBoolean()
    }
}