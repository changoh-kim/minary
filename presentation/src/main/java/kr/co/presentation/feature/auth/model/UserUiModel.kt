package kr.co.presentation.feature.auth.model

import androidx.compose.runtime.Immutable


@Immutable
data class UserUiModel(
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
)