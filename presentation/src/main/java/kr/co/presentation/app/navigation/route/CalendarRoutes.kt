package kr.co.presentation.app.navigation.route

import kotlinx.serialization.Serializable

@Serializable
data object CalendarRoute

@Serializable
data class MonthlyCalendarRoute(val year: Int, val month: Int)

@Serializable
data class YearlyCalendarRoute(val year: Int)
