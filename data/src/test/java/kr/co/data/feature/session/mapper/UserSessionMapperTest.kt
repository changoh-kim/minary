package kr.co.data.feature.session.mapper

import kr.co.data.feature.session.mapper.UserSessionMapper.toUserSession
import kr.co.data.feature.session.mapper.UserSessionMapper.toUserSessionModel
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserSessionMapperTest : BaseDataUnitTest() {
    @Test
    fun `maps session model to domain session`() {
        assertEquals(DataFixtures.userSession, DataFixtures.userSessionModel.toUserSession())
    }

    @Test
    fun `maps domain session to session model`() {
        assertEquals(DataFixtures.userSessionModel, DataFixtures.userSession.toUserSessionModel())
    }
}
