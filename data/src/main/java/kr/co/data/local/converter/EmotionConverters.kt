package kr.co.data.local.converter

import androidx.room.TypeConverter
import kr.co.domain.common.extension.toEmotion
import kr.co.domain.feature.emotion.Emotion


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