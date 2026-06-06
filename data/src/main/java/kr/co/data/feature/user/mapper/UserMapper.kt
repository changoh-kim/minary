package kr.co.data.feature.user.mapper

import kr.co.data.feature.user.model.UserModel
import kr.co.domain.feature.user.model.User

object UserMapper {
    fun User.toUserModel() = UserModel(
        uid = uid,
        email = email,
    )

    fun UserModel.toUser() = User(
        uid = uid,
        email = email,
    )
}