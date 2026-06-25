package kr.co.presentation.feature.dashboard.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kr.co.core.common.model.Emotion

@Immutable
@Parcelize
data class DashboardUiModel(
    val recentDiaries: List<DashboardDiaryUiModel?> = emptyList(),
    val totalDiaryCount: Int = 0,
    val weeklyDiaryCount: Int = 0,
    val totalWordCount: Int = 0,
    val longestStreak: Int = 0,
    val emotionCounts: Map<Emotion, Int> = emptyMap()
) : Parcelable
