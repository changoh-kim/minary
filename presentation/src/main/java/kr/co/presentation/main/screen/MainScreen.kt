package kr.co.presentation.main.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kr.co.presentation.R
import kr.co.presentation.common.extension.getString
import kr.co.presentation.feature.calendar.navigation.MonthlyCalendarRoute
import kr.co.presentation.feature.dashboard.navigation.DashboardRoute
import kr.co.presentation.feature.setting.navigation.SettingRoute
import kr.co.presentation.feature.store.navigation.StoreRoute
import kr.co.presentation.main.navigation.MainHost
import kr.co.presentation.main.navigation.MainNavigationItem
import kr.co.presentation.main.viewmodel.MainSideEffect
import kr.co.presentation.main.viewmodel.MainViewModel
import kr.co.presentation.navigation.extension.navigateIfNotCurrent
import kr.co.presentation.theme.MinaryTheme
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.LocalDate
import java.time.YearMonth


@SuppressLint("RestrictedApi")
@Composable
fun MainScreen(
    onNavigateToDiaryScreen: (LocalDate) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val navController = rememberNavController()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MainSideEffect.NavigateToDiaryScreen -> {
                onNavigateToDiaryScreen(sideEffect.date)
            }

            is MainSideEffect.ShowMsg -> coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
            }
        }
    }

    MainContent(
        snackbarHostState = snackbarHostState,
        navController = navController,
    ) { innerPadding ->
        val currentYearMonth = YearMonth.now()
        MainHost(
            navController = navController,
            startDestination = MonthlyCalendarRoute(currentYearMonth.year, currentYearMonth.monthValue),
            modifier = Modifier.padding(innerPadding),
            viewModel::handleIntent,
        )
    }
}

@Composable
fun MainContent(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navController: NavHostController = rememberNavController(),
    content: @Composable (PaddingValues) -> Unit = {}
) {
    val navigationItems = remember {
        listOf(
            MainNavigationItem(R.string.calendar, MonthlyCalendarRoute(), Icons.Filled.DateRange),
            MainNavigationItem(R.string.dashboard, DashboardRoute, Icons.Filled.Star),
            MainNavigationItem(R.string.store, StoreRoute, Icons.Filled.ShoppingCart),
            MainNavigationItem(R.string.setting, SettingRoute, Icons.Filled.Settings),
        )
    }

    Scaffold(
        topBar = { TopBar() },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomAppBar {
                navigationItems.forEach { navItem ->
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    NavigationBarItem(
                        icon = { Icon(navItem.icon, stringResource(navItem.labelResId)) },
                        label = { Text(stringResource(navItem.labelResId)) },
                        selected = (
                                currentDestination?.hierarchy?.any {
                                    it.hasRoute(navItem.route::class)
                                } == true),
                        onClick = {
                            navController.navigateIfNotCurrent(navItem.route, currentDestination)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        actions = {
            IconButton(
                onClick = {}
            ) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = stringResource(R.string.search)
                )
            }
        }
    )
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun MainContentPreview() {
    MinaryTheme {
        MainContent { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.preview_main_content))
            }
        }
    }
}