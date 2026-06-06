package kr.co.presentation.main.model

import androidx.compose.runtime.Immutable

@Immutable
data class UserSessionUiModel(
    val uid: String = "",
    val email: String = "",
)
