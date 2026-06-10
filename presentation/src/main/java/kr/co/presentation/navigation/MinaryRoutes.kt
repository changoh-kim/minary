package kr.co.presentation.navigation

import kotlinx.serialization.Serializable


@Serializable data object WelcomeRoute
@Serializable data object SignInRoute
@Serializable data object SignUpRoute
@Serializable data object AccountDeletionRoute
@Serializable data object HomeRoute
@Serializable data class DiaryPreviewRoute(val year: Int, val month: Int, val date: Int)
@Serializable data class DiaryEditRoute(val year: Int, val month: Int, val date: Int, val isNewDiary: Boolean)
@Serializable data class MonthlyCalendarRoute(val year: Int, val month: Int)
@Serializable data class YearlyCalendarRoute(val year: Int)
@Serializable data object DashboardRoute
@Serializable data object StoreRoute
@Serializable data object SettingsRoute
@Serializable data object ProfileDetailRoute
@Serializable data object ProfileEditRoute