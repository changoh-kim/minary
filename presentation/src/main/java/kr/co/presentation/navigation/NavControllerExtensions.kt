package kr.co.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy


// 이동하려는 현재 목적지와 다른 계층 구조를 가진 경우에만 이동.
fun NavController.navigateIfNotCurrent(route: Any, currentDestination: NavDestination?) {
    if (currentDestination?.hierarchy?.any { it.hasRoute(route::class) } == true) {
        return
    }

    this.navigate(route) {
        launchSingleTop = true
        restoreState = true
    }
}