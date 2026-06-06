package kr.co.presentation.feature.dashboard.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kr.co.domain.feature.emotion.model.Emotion
import kr.co.presentation.feature.diary.model.DiaryUiModel


@Immutable
@Parcelize
data class DashboardUiModel(
    val recentDiaries: List<DiaryUiModel?> = emptyList(),
    val totalDiaryCount: Int = 0,
    val totalWordCount: Int = 0,
    val mostFrequentEmotion: Emotion = Emotion.UNKNOWN,
    val leastFrequentEmotion: Emotion = Emotion.UNKNOWN
) : Parcelable