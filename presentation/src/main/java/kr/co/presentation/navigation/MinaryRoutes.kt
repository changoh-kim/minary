package kr.co.presentation.navigation

import kotlinx.serialization.Serializable


@Serializable data object WelcomeRoute
@Serializable data object LoginRoute
@Serializable data object SignUpRoute
@Serializable data object MainRoute
@Serializable data class DiaryRoute(val year: Int, val month: Int, val date: Int)
@Serializable data class MonthlyCalendarRoute(val year: Int, val month: Int)
@Serializable data class YearlyCalendarRoute(val year: Int)
@Serializable data object DashboardRoute
@Serializable data object StoreRoute
@Serializable data object SettingRoute