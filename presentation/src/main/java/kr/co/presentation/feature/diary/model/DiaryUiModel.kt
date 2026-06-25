package kr.co.presentation.feature.diary.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kr.co.core.common.model.Emotion
import kr.co.core.common.state.DiarySyncStatus
import kr.co.core.ui.common.parceler.LocalDateParceler
import java.time.LocalDate
import java.util.UUID

@Immutable
@Parcelize
@TypeParceler<LocalDate, LocalDateParceler>()
data class DiaryUiModel(
    val id: String = UUID.randomUUID().toString(),

    val date: LocalDate = LocalDate.now(),
    val title: String = "",
    val content: String = "",
    val emotions: List<Emotion> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: DiarySyncStatus = DiarySyncStatus.PENDING_CREATE,
) : Parcelable