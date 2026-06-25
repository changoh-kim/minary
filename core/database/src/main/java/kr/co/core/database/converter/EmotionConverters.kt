package kr.co.core.database.converter

import androidx.room.TypeConverter
import kr.co.core.common.extension.toEmotion
import kr.co.core.common.model.Emotion


object EmotionConverters {

    @TypeConverter
    fun fromString(value: String?): Emotion? {
        return value?.toEmotion()
    }

    @TypeConverter
    fun toString(value: Emotion?): String? {
        return value?.name
    }
}