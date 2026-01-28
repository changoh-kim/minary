package kr.co.domain.model.diary

import kr.co.domain.model.emotion.Emotion


data class UpsertResultDiaryData(
    val id: Long = 0L,
    val emotion: Emotion = Emotion.UNKNOWN,
)