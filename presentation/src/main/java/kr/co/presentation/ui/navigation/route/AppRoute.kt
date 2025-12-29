package kr.co.presentation.ui.navigation.route

import kotlinx.serialization.Serializable
import java.time.YearMonth


@Serializable
sealed interface AppRoute

// Auth
@Serializable
data object AuthGraph : AppRoute
@Serializable
data object Welcome : AppRoute
@Serializable
data object Login : AppRoute
@Serializable
data object SignUp : AppRoute

// Main
@Serializable
data object MainGraph : AppRoute
@Serializable
data object Main : AppRoute
@Serializable
data class Diary(val year: Int, val month: Int, val date: Int) : AppRoute

// Calendar
@Serializable
data object CalendarGraph : AppRoute
@Serializable
data class MonthlyCalendar(val year: Int, val month: Int) : AppRoute
@Serializable
data class YearlyCalender(val year: Int) : AppRoute

// DashBoard
@Serializable
data object DashBoardGraph : AppRoute
@Serializable
data object DashBoard : AppRoute

// Store
@Serializable
data object StoreGraph : AppRoute
@Serializable
data object Store : AppRoute

// Setting
@Serializable
data object SettingGraph : AppRoute
@Serializable
data object Setting : AppRoute