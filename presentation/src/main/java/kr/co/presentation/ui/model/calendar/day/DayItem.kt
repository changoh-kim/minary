package kr.co.presentation.ui.model.calendar.day

import androidx.compose.runtime.Immutable
import kr.co.presentation.ui.model.calendar.BaseItem
import java.time.LocalDate


@Immutable
sealed interface DayItem : BaseItem {
    val date: LocalDate
}