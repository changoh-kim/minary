package kr.co.presentation.ui.navigation.graph

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kr.co.presentation.ui.navigation.route.CalendarGraph
import kr.co.presentation.ui.navigation.route.MonthlyCalendar
import kr.co.presentation.ui.navigation.route.YearlyCalender
import kr.co.presentation.ui.screen.contents.calendar.MonthlyCalendarScreen
import kr.co.presentation.ui.screen.contents.calendar.YearlyCalendarScreen
import kr.co.presentation.viewmodel.main.MainIntent
import java.time.YearMonth


internal fun NavGraphBuilder.calenderNavGraph(
    navController: NavHostController,
    intent: (MainIntent) -> Unit = {},
) {
    val currentYearMonth = YearMonth.now()

    navigation<CalendarGraph>(
        startDestination = MonthlyCalendar(
            year = currentYearMonth.year,
            month = currentYearMonth.monthValue
        )
    ) {

        composable<MonthlyCalendar> {
            MonthlyCalendarScreen(
                onNavigateToDiaryScreen = { date ->
                    intent(MainIntent.NavigateToDiaryScreen(date))
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
            YearlyCalendarScreen(
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
