package kr.co.presentation.ui.model.calendar.yearmonth

import androidx.compose.runtime.Immutable
import kr.co.presentation.ui.model.calendar.BaseItem
import java.time.Year


@Immutable
data class YearItem(
    val year: Year = Year.now(),

    override val key: String = year.toString(),
    override val contentType: BaseItem.ContentType = BaseItem.ContentType.YEAR,
) : YearMonthItem