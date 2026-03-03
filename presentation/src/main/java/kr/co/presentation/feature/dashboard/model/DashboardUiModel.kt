package kr.co.presentation.feature.dashboard.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kr.co.domain.feature.emotion.Emotion


@Immutable
@Parcelize
data class DashboardUiModel(
    val recentEmotions: List<Emotion> = emptyList(),
    val totalDiaries: Int = 0,
    val totalWords: Int = 0,
    val dominantEmotion: Emotion = Emotion.UNKNOWN,
    val rarestEmotion: Emotion = Emotion.UNKNOWN
) : Parcelable