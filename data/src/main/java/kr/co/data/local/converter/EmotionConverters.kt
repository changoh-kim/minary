package kr.co.data.local.converter

import androidx.room.TypeConverter
import kr.co.domain.common.Converter
import kr.co.domain.feature.emotion.Emotion


object EmotionConverters {

    @TypeConverter
    fun fromString(value: String?): Emotion? {
        return value?.let { Converter.toEmotion(it) }
    }

    @TypeConverter
    fun toString(value: Emotion?): String? {
        return value?.let { Converter.toEmotionName(it) }
    }
}