package kr.co.presentation.ui.model.calendar.yearmonth

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kr.co.presentation.ui.model.calendar.BaseItem
import kr.co.presentation.ui.model.calendar.day.DayItem
import java.time.YearMonth


@Immutable
data class MonthItem(
    val yearMonth: YearMonth = YearMonth.now(),
    val days: ImmutableList<DayItem> = persistentListOf(),

    override val key: String = "${yearMonth.year}-${yearMonth.monthValue}",
    override val contentType: BaseItem.ContentType = BaseItem.ContentType.MONTH,
) : YearMonthItem