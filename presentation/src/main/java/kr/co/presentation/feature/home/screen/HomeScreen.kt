package kr.co.presentation.feature.home.screen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kr.co.domain.common.state.SyncProcessState
import kr.co.presentation.R
import kr.co.presentation.design.ThemePreviews
import kr.co.presentation.feature.home.navigation.HomeHost
import kr.co.presentation.feature.home.navigation.HomeNavigationItem
import kr.co.presentation.feature.home.preview.HomeScreenPreviewDataProvider
import kr.co.presentation.feature.home.viewmodel.HomeAction
import kr.co.presentation.feature.home.viewmodel.HomeScreenState
import kr.co.presentation.feature.home.viewmodel.HomeViewModel
import kr.co.presentation.navigation.DashboardRoute
import kr.co.presentation.navigation.MinaryAppState
import kr.co.presentation.navigation.MonthlyCalendarRoute
import kr.co.presentation.navigation.SettingsRoute
import kr.co.presentation.navigation.StoreRoute
import kr.co.presentation.navigation.navigateIfNotCurrent
import kr.co.presentation.theme.MinaryTheme
import org.orbitmvi.orbit.compose.collectAsState
import java.time.YearMonth

@SuppressLint("RestrictedApi")
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    appState: MinaryAppState,
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val bottomNavController = rememberNavController()

    HomeContent(
        state = state,
        onAction = viewModel::handleAction,
        snackbarHostState = snackbarHostState,
        navController = bottomNavController,
    ) { innerPadding ->
        val currentYearMonth = YearMonth.now()

        HomeHost(
            appState = appState,
            navController = bottomNavController,
            startDestination = MonthlyCalendarRoute(
                currentYearMonth.year,
                currentYearMonth.monthValue
            ),
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun HomeContent(
    state: HomeScreenState = HomeScreenState(),
    onAction: (HomeAction) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navController: NavHostController = rememberNavController(),
    content: @Composable (PaddingValues) -> Unit = {}
) {
    val currentYearMonth = YearMonth.now()
    val navigationItems = remember {
        listOf(
            HomeNavigationItem(
                R.string.calendar,
                MonthlyCalendarRoute(currentYearMonth.year, currentYearMonth.monthValue),
                Icons.Outlined.CalendarMonth
            ),
            HomeNavigationItem(
                R.string.dashboard,
                DashboardRoute,
                Icons.Outlined.Dashboard
            ),
            HomeNavigationItem(
                R.string.search,
                StoreRoute,
                Icons.Outlined.Search
            ),
            HomeNavigationItem(
                R.string.setting,
                SettingsRoute,
                Icons.Outlined.Settings
            ),
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        InitDiarySyncOverlay(
            processState = state.initDiarySyncProcessState,
            onRetry = { onAction(HomeAction.RetryClicked) }
        )

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    navigationItems.forEach { navItem ->
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        val isSelected =
                            currentDestination?.hierarchy?.any { it.hasRoute(navItem.route::class) } == true

                        val contentColor =
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = navItem.icon,
                                    contentDescription = stringResource(navItem.labelResId),
                                    modifier = Modifier.size(26.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(navItem.labelResId).uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    letterSpacing = 0.5.sp
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                navController.navigateIfNotCurrent(
                                    navItem.route,
                                    currentDestination
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = contentColor,
                                selectedTextColor = contentColor,
                                unselectedIconColor = contentColor,
                                unselectedTextColor = contentColor,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            content(innerPadding)
        }
    }
}

@Composable
private fun InitDiarySyncOverlay(
    processState: SyncProcessState,
    onRetry: () -> Unit = {},
) {
    val isVisible =
        processState is SyncProcessState.InProgress.Determinate || processState is SyncProcessState.Failed

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f) // 최상단에 위치
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (processState) {
                is SyncProcessState.InProgress.Determinate -> {
                    CircularProgressIndicator(
                        progress = { processState.progress },
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.initial_sync_in_progress),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${(processState.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is SyncProcessState.Failed -> {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.initial_sync_failed),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.initial_sync_failed_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onRetry) {
                        Text(stringResource(R.string.initial_sync_retry))
                    }
                }

                else -> {}
            }
        }
    }
}

@ThemePreviews
@Composable
private fun HomeScreenPreview(
    @PreviewParameter(HomeScreenPreviewDataProvider::class)
    state: HomeScreenState
) {
    HomeScreenPreviewContent(state)
}

@Composable
fun HomeScreenPreviewContent(
    state: HomeScreenState
) {
    MinaryTheme {
        HomeContent(
            state = state,
            content = { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.preview_main_content))
                }
            },
        )
    }
}