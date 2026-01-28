package kr.co.presentation.mapper

import kr.co.domain.model.auth.User
import kr.co.presentation.ui.model.common.UiUser


object UserMapper {
    fun User.toUiUser() = UiUser(
        name = this.name,
        email = this.email,
        photoUrl = this.photoUrl,
    )
}