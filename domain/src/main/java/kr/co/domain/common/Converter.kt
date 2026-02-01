package kr.co.domain.common

import kr.co.domain.feature.emotion.Emotion
import kr.co.domain.feature.emotion.Emotion.UNKNOWN
import kr.co.domain.feature.emotion.Emotion.entries
import java.time.LocalDate
import java.time.format.DateTimeFormatter


object Converter {
    fun toDateString(date: LocalDate): String = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    fun toDate(dateStr: String): LocalDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)

    fun toEmotionName(emotion: Emotion) = emotion.name
    fun toEmotion(emotionName: String): Emotion {
        val normalized = emotionName.trim()
        return entries.find {
            it.name.equals(normalized, ignoreCase = true)
        } ?: UNKNOWN
    }

    fun toInt(booleanValue: Boolean) = if (booleanValue) 1 else 0
    fun toBoolean(intValue: Int) = intValue == 1
}