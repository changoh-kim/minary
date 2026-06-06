package kr.co.presentation.common.extension

import androidx.compose.ui.graphics.Color
import kr.co.domain.feature.emotion.model.Emotion
import kr.co.presentation.R


val Emotion.color: Color
    get() = when (this) {
        // 1. 긍정 & 활력 (Positive & High Energy)
        Emotion.JOY -> Color(0xFFFFD700)                    // Gold
        Emotion.EXCITEMENT -> Color(0xFFFF8C00)             // Dark Orange
        Emotion.TRIUMPH -> Color(0xFFDAA520)                // Goldenrod
        Emotion.AMUSEMENT -> Color(0xFFFF69B4)              // Hot Pink

        // 2. 사랑 & 애정 (Love & Affection)
        Emotion.ROMANCE -> Color(0xFFFF1493)                // Deep Pink
        Emotion.ADORATION -> Color(0xFFFFB6C1)              // Light Pink
        Emotion.SEXUAL_DESIRE -> Color(0xFF800020)          // Burgundy

        // 3. 차분함 & 심미 (Calm & Aesthetic)
        Emotion.CALMNESS -> Color(0xFF98FB98)               // Pale Green
        Emotion.SATISFACTION -> Color(0xFF2E8B57)           // Sea Green
        Emotion.AESTHETIC_APPRECIATION -> Color(0xFFE6E6FA) // Lavender
        Emotion.ENTRANCEMENT -> Color(0xFF9370DB)           // Medium Purple

        // 4. 존경 & 관심 (Respect & Interest)
        Emotion.ADMIRATION -> Color(0xFF4682B4)             // Steel Blue
        Emotion.AWE -> Color(0xFF4B0082)                    // Indigo
        Emotion.INTEREST -> Color(0xFF00CED1)               // Dark Turquoise

        // 5. 슬픔 & 그리움 (Sadness & Longing)
        Emotion.SADNESS -> Color(0xFF4169E1)                // Royal Blue
        Emotion.NOSTALGIA -> Color(0xFFD2691E)              // Chocolate/Sepia
        Emotion.SYMPATHY -> Color(0xFFFFA07A)               // Light Salmon
        Emotion.EMPATHETIC_PAIN -> Color(0xFFBC8F8F)        // Rosy Brown

        // 6. 부정 & 경계 (Negative & Alert)
        Emotion.ANGER -> Color(0xFFFF0000)                  // Red
        Emotion.FEAR -> Color(0xFF2F4F4F)                   // Dark Slate Gray
        Emotion.HORROR -> Color(0xFF000000)                 // Black
        Emotion.ANXIETY -> Color(0xFF708090)                // Slate Gray

        // 7. 복합 & 모호 (Complex & Ambiguous)
        Emotion.CONFUSION -> Color(0xFFD8BFD8)              // Thistle
        Emotion.BOREDOM -> Color(0xFFA9A9A9)                // Dark Gray
        Emotion.AWKWARDNESS -> Color(0xFFBDB76B)            // Dark Khaki
        Emotion.DISGUST -> Color(0xFF556B2F)                // Dark Olive Green
        Emotion.ENVY -> Color(0xFF32CD32)                   // Lime Green

        // 8. 욕구 (Desire)
        Emotion.CRAVING -> Color(0xFFFF4500)                // Orange Red

        // 9. 미정 및 기본값 (Undefined / Default)
        Emotion.UNKNOWN -> Color(0xFFC0C0C0)                // Silver
    }

val Emotion.resId: Int
    get() = when (this) {
        // 1. 긍정 & 활력 (Positive & High Energy)
        Emotion.JOY -> R.string.emotion_joy
        Emotion.EXCITEMENT -> R.string.emotion_excitement
        Emotion.TRIUMPH -> R.string.emotion_triumph
        Emotion.AMUSEMENT -> R.string.emotion_amusement

        // 2. 사랑 & 애정 (Love & Affection)
        Emotion.ROMANCE -> R.string.emotion_romance
        Emotion.ADORATION -> R.string.emotion_adoration
        Emotion.SEXUAL_DESIRE -> R.string.emotion_sexual_desire

        // 3. 차분함 & 심미 (Calm & Aesthetic)
        Emotion.CALMNESS -> R.string.emotion_calmness
        Emotion.SATISFACTION -> R.string.emotion_satisfaction
        Emotion.AESTHETIC_APPRECIATION -> R.string.emotion_aesthetic_appreciation
        Emotion.ENTRANCEMENT -> R.string.emotion_entrancement

        // 4. 존경 & 관심 (Respect & Interest)
        Emotion.ADMIRATION -> R.string.emotion_admiration
        Emotion.AWE -> R.string.emotion_awe
        Emotion.INTEREST -> R.string.emotion_interest

        // 5. 슬픔 & 그리움 (Sadness & Longing)
        Emotion.SADNESS -> R.string.emotion_sadness
        Emotion.NOSTALGIA -> R.string.emotion_nostalgia
        Emotion.SYMPATHY -> R.string.emotion_sympathy
        Emotion.EMPATHETIC_PAIN -> R.string.emotion_empathetic_pain

        // 6. 부정 & 경계 (Negative & Alert)
        Emotion.ANGER -> R.string.emotion_anger
        Emotion.FEAR -> R.string.emotion_fear
        Emotion.HORROR -> R.string.emotion_horror
        Emotion.ANXIETY -> R.string.emotion_anxiety

        // 7. 복합 & 모호 (Complex & Ambiguous)
        Emotion.CONFUSION -> R.string.emotion_confusion
        Emotion.BOREDOM -> R.string.emotion_boredom
        Emotion.AWKWARDNESS -> R.string.emotion_awkwardness
        Emotion.DISGUST -> R.string.emotion_disgust
        Emotion.ENVY -> R.string.emotion_envy

        // 8. 욕구 (Desire)
        Emotion.CRAVING -> R.string.emotion_craving

        // 9. 미정 및 기본값 (Undefined / Default)
        Emotion.UNKNOWN -> R.string.emotion_unknown
    }