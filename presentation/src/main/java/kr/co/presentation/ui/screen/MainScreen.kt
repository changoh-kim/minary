package kr.co.presentation.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import kr.co.presentation.ui.navigation.AppRoute.NavigationItem
import kr.co.presentation.ui.navigation.host.contents.CalenderNavHost
import kr.co.presentation.ui.navigation.host.contents.DashBoardNavHost
import kr.co.presentation.ui.navigation.host.contents.SettingNavHost
import kr.co.presentation.ui.navigation.host.contents.StoreNavHost
import kr.co.presentation.ui.theme.MinaryTheme


@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navigationItems = listOf(
        NavigationItem.Calender,
        NavigationItem.DashBoard,
        NavigationItem.Store,
        NavigationItem.Setting
    )

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
private fun MainScreenPreview() {
    MinaryTheme {
        MainScreen()
    }
}
