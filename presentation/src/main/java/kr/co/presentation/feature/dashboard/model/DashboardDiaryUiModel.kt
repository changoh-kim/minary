package kr.co.presentation.feature.dashboard.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kr.co.core.common.model.Emotion
import kr.co.core.ui.common.parceler.LocalDateParceler
import java.time.LocalDate

@Immutable
@Parcelize
@TypeParceler<LocalDate, LocalDateParceler>()
data class DashboardDiaryUiModel(
    val date: LocalDate = LocalDate.now(),
    val emotions: List<Emotion> = emptyList(),
) : Parcelable
