package kr.co.presentation.feature.diary.navigation

import kotlinx.serialization.Serializable


@Serializable
data class DiaryRoute(
    val year: Int,
    val month: Int,
    val date: Int
)