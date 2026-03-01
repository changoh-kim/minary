package kr.co.data.local.converter

import androidx.room.TypeConverter
import kr.co.domain.common.extension.toBoolean
import kr.co.domain.common.extension.toInt


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