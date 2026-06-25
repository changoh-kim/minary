package kr.co.presentation.feature.search.model

import androidx.compose.runtime.Immutable
import kr.co.core.common.model.Emotion
import java.time.LocalDate

@Immutable
data class SearchDiaryUiModel(
    val date: LocalDate = LocalDate.now(),
    val title: String = "",
    val content: String = "",
    val emotions: List<Emotion> = emptyList(),
    val imageUrls: List<String> = emptyList(),
)
