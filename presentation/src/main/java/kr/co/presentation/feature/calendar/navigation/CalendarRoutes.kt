package kr.co.presentation.feature.calendar.navigation

import kotlinx.serialization.Serializable
import java.time.YearMonth


@Serializable data class MonthlyCalendarRoute(
    val year: Int = YearMonth.now().year,
    val month: Int = YearMonth.now().monthValue
)
@Serializable data class YearlyCalenderRoute(val year: Int)