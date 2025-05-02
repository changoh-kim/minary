package kr.co.presentation.ui.screen

import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import kr.co.presentation.ui.navigation.host.contents.CalenderNavHost
import kr.co.presentation.ui.navigation.host.contents.DashBoardNavHost
import kr.co.presentation.ui.navigation.host.contents.SettingNavHost
import kr.co.presentation.ui.navigation.host.contents.StoreNavHost
import kr.co.presentation.ui.theme.MinaryTheme
import kr.co.presentation.viewmodel.MainContainerViewModel

sealed class NavigationItem(
    val route: String,
    val icon: ImageVector,
    val name: String
) {
    object Calender : NavigationItem("calender", Icons.Filled.DateRange, "캘린더")
    object DashBoard : NavigationItem("dashboard", Icons.Filled.Star, "대시보드")
    object Store : NavigationItem("store", Icons.Filled.ShoppingCart, "상점")
    object Setting : NavigationItem("setting", Icons.Filled.Settings, "환경설정")
}

@Composable
fun MainContainerScreen(
    mainContainerViewModel: MainContainerViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navigationItems = listOf(
        NavigationItem.Calender,
        NavigationItem.DashBoard,
        NavigationItem.Store,
        NavigationItem.Setting
    )

    // 
    var selectedItem by rememberSaveable { mutableStateOf(NavigationItem.Calender.route) }

    Scaffold(
        topBar = { TopBar() },
        bottomBar = {
            BottomAppBar {
                navigationItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, item.name) },
                        label = { Text(item.name) },
                        selected = item.route == selectedItem,
                        onClick = {
                            selectedItem = item.route
                        }
                    )
                }
            }
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            when(selectedItem) {
                NavigationItem.Calender.route -> CalenderNavHost()
                NavigationItem.DashBoard.route -> DashBoardNavHost()
                NavigationItem.Store.route -> StoreNavHost()
                NavigationItem.Setting.route -> SettingNavHost()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
    TopAppBar(
        title = { Text("Minary") },
        actions = {
            IconButton(
                onClick = {}
            ) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun MainContentsScreenPreview() {
    MinaryTheme {
        MainContainerScreen()
    }
}
