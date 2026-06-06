package kr.co.data.feature.session.mapper

import kr.co.data.feature.session.model.UserSessionModel
import kr.co.domain.feature.session.model.UserSession

object UserSessionMapper {
    fun UserSession.toUserSessionModel() = UserSessionModel(
        uid = uid,
        email = email,
    )

    fun UserSessionModel.toUserSession() = UserSession(
        uid = uid,
        email = email,
    )
}