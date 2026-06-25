package kr.co.core.database.model

import kr.co.core.common.model.Emotion

data class EmotionStats(
    val emotion: Emotion,
    val count: Int
)