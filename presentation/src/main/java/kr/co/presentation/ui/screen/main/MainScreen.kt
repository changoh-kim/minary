package kr.co.presentation.ui.screen.main

import android.annotation.SuppressLint
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kr.co.presentation.R
import kr.co.presentation.ui.navigation.extensions.navigateIfNotCurrent
import kr.co.presentation.ui.navigation.host.MainNavHost
import kr.co.presentation.ui.navigation.item.NavigationItem
import kr.co.presentation.ui.navigation.route.CalendarGraph
import kr.co.presentation.ui.navigation.route.DashBoardGraph
import kr.co.presentation.ui.navigation.route.Diary
import kr.co.presentation.ui.navigation.route.SettingGraph
import kr.co.presentation.ui.navigation.route.StoreGraph
import kr.co.presentation.ui.theme.MinaryTheme


@SuppressLint("RestrictedApi")
@Composable
fun MainScreen(
    onNavigateToDiaryScreen: (Diary) -> Unit,
    // mainViewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    val navigationItems = remember {
        listOf(
            NavigationItem(R.string.calendar, CalendarGraph, Icons.Filled.DateRange),
            NavigationItem(R.string.dashboard, DashBoardGraph, Icons.Filled.Star),
            NavigationItem(R.string.store, StoreGraph, Icons.Filled.ShoppingCart),
            NavigationItem(R.string.setting, SettingGraph, Icons.Filled.Settings),
        )
    }

    Scaffold(
        topBar = { TopBar() },
        bottomBar = {
            BottomAppBar {
                navigationItems.forEach { item ->
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    NavigationBarItem(
                        icon = { Icon(item.icon, stringResource(item.labelResId)) },
                        label = { Text(stringResource(item.labelResId)) },
                        selected = (
                                currentDestination?.hierarchy?.any {
                                    it.hasRoute(item.route::class)
                                } == true),
                        onClick = {
                            navController.navigateIfNotCurrent(item.route, currentDestination)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        MainNavHost(
            navController = navController,
            startDestination = CalendarGraph,
            modifier = Modifier.padding(innerPadding),
            onNavigateToDiaryScreen = onNavigateToDiaryScreen
        )
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
private fun MainScreenPreview() {
    MinaryTheme {
        MainScreen(
            onNavigateToDiaryScreen = {}
        )
    }
}