package kr.co.presentation.main.mapper

import kr.co.domain.feature.session.model.UserSession
import kr.co.presentation.main.model.UserSessionUiModel

object UserSessionUiModelMapper {
    fun UserSession.toUserSessionUiModel() = UserSessionUiModel(
        uid = uid,
        email = email,
    )

    fun UserSessionUiModel.toUserSession() = UserSession(
        uid = uid,
        email = email,
    )
}