package kr.co.data.feature.emotion.source.local.model

import kr.co.domain.feature.emotion.model.Emotion

data class EmotionStats(
    val emotion: Emotion,
    val count: Int
)