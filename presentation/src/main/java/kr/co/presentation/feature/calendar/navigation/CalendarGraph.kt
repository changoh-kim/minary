package kr.co.presentation.feature.calendar.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kr.co.presentation.feature.calendar.screen.monthly.MonthlyCalendarScreen
import kr.co.presentation.feature.calendar.screen.yearly.YearlyCalendarScreen
import kr.co.presentation.app.navigation.MinaryAppState
import kr.co.presentation.app.navigation.route.CalendarRoute
import kr.co.presentation.app.navigation.route.MonthlyCalendarRoute
import kr.co.presentation.app.navigation.route.YearlyCalendarRoute
import java.time.YearMonth

internal fun NavGraphBuilder.calendarGraph(
    appState: MinaryAppState,
    navController: NavHostController,
) {
    navigation<CalendarRoute>(
        startDestination = MonthlyCalendarRoute(YearMonth.now().year, YearMonth.now().monthValue)
    ) {
        composable<MonthlyCalendarRoute> {
            MonthlyCalendarScreen(
                onYearClicked = { year ->
                    navController.navigate(YearlyCalendarRoute(year)) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onDayClicked = { date -> appState.navigateToDiaryDetail(date) },
                /*onNavigateToEdit = { date, isNewDiary -> appState.navigateToDiaryEdit(date, isNewDiary) }*/
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
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
