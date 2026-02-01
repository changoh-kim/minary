package kr.co.presentation.feature.auth.mapper

import kr.co.domain.feature.auth.model.User
import kr.co.presentation.feature.auth.model.UserUiModel


object UserUiModelMapper {

    fun User.toUserUiModel() = UserUiModel(
        name = this.name,
        email = this.email,
        photoUrl = this.photoUrl,
    )

    fun UserUiModel.toUser() = User(
        name = this.name,
        email = this.email,
        photoUrl = this.photoUrl,
    )
}