package kr.co.presentation.feature.calendar.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kr.co.presentation.feature.calendar.screen.MonthlyCalendarScreen
import kr.co.presentation.feature.calendar.screen.YearlyCalendarScreen
import kr.co.presentation.main.viewmodel.MainIntent


internal fun NavGraphBuilder.calenderGraph(
    navController: NavHostController,
    intent: (MainIntent) -> Unit = {},
) {

    composable<MonthlyCalendarRoute> {
        MonthlyCalendarScreen(
            onNavigateToDiaryScreen = { date ->
                intent(MainIntent.NavigateToDiaryScreen(date))
            },
            onNavigateToYearlyCalendar = { year ->
                navController.navigate(YearlyCalenderRoute(year)) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
        )
    }

    composable<YearlyCalenderRoute> {
        YearlyCalendarScreen(
            onNavigateToMonthlyCalendar = { yearMonth ->
                navController.navigate(MonthlyCalendarRoute(yearMonth.year, yearMonth.monthValue)) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                }
            }
        )
    }
}