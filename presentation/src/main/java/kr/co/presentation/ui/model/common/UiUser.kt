package kr.co.presentation.ui.model.common

import androidx.compose.runtime.Immutable


@Immutable
data class UiUser(
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
)