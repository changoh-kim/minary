package kr.co.presentation.feature.calendar.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kr.co.presentation.feature.calendar.screen.MonthlyCalendarScreen
import kr.co.presentation.feature.calendar.screen.YearlyCalendarScreen
import kr.co.presentation.navigation.MinaryAppState
import kr.co.presentation.navigation.MonthlyCalendarRoute
import kr.co.presentation.navigation.YearlyCalendarRoute


internal fun NavGraphBuilder.calenderGraph(
    appState: MinaryAppState,
    navController: NavHostController,
) {
    composable<MonthlyCalendarRoute> {
        MonthlyCalendarScreen(
            onYearClicked = { year ->
                navController.navigate(YearlyCalendarRoute(year)) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onDayClicked = { date -> appState.navigateToDiaryPreview(date) },
        )
    }

    composable<YearlyCalendarRoute> {
        YearlyCalendarScreen(
            onMonthClicked = { yearMonth ->
                navController.navigate(MonthlyCalendarRoute(yearMonth.year, yearMonth.monthValue)) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                }
            }
        )
    }
}