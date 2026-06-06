package kr.co.presentation.feature.account.model

import androidx.compose.runtime.Immutable

@Immutable
data class AccountUiModel(
    val uid: String = "",
    val email: String = "",
)
