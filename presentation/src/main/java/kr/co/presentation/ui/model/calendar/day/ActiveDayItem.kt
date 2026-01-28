package kr.co.presentation.ui.model.calendar.day

import androidx.compose.runtime.Immutable
import kr.co.domain.model.emotion.Emotion
import kr.co.presentation.ui.model.calendar.BaseItem
import java.time.LocalDate


@Immutable
data class ActiveDayItem(
    override val date: LocalDate = LocalDate.now(),

    override val key: String = "${date.year}-${date.monthValue}-${date.dayOfMonth}",
    override val contentType: BaseItem.ContentType = BaseItem.ContentType.DATE,
) : DayItem