package kr.co.presentation.app.navigation.route

import kotlinx.serialization.Serializable

@Serializable
data class DiaryDetailRoute(val year: Int, val month: Int, val date: Int)

@Serializable
data class DiaryEditRoute(val year: Int, val month: Int, val date: Int, val isNewDiary: Boolean)
