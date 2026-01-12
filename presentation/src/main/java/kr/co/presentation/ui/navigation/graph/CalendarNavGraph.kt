package kr.co.presentation.ui.navigation.graph

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kr.co.presentation.ui.navigation.route.CalendarGraph
import kr.co.presentation.ui.navigation.route.MonthlyCalendar
import kr.co.presentation.ui.navigation.route.YearlyCalender
import kr.co.presentation.ui.screen.contents.calendar.MonthlyCalendarScreen
import kr.co.presentation.ui.screen.contents.calendar.YearlyCalendarScreen
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth


internal fun NavGraphBuilder.calenderNavGraph(
    navController: NavHostController,
    onNavigateToDiaryScreen: (LocalDate) -> Unit,
) {
    val currentYearMonth = YearMonth.now()
    val currentYear = currentYearMonth.year
    val currentMonth = currentYearMonth.monthValue

    navigation<CalendarGraph>(startDestination = MonthlyCalendar(currentYear, currentMonth)) {

        composable<MonthlyCalendar> { backStackEntry ->
            val monthCalender: MonthlyCalendar = backStackEntry.toRoute()
            MonthlyCalendarScreen(
                yearMonth = YearMonth.of(monthCalender.year, monthCalender.month),
                onNavigateToDiaryScreen = { date ->
                    onNavigateToDiaryScreen(date)
                },
                onNavigateToYearlyCalendar = { year ->
                    navController.navigate(YearlyCalender(year)) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }

        composable<YearlyCalender> { backStackEntry ->
            val yearlyCalender: YearlyCalender = backStackEntry.toRoute()
            YearlyCalendarScreen(
                year = Year.of(yearlyCalender.year),
                onNavigateToMonthlyCalendar = { yearMonth ->
                    navController.navigate(MonthlyCalendar(yearMonth.year, yearMonth.monthValue)) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}
