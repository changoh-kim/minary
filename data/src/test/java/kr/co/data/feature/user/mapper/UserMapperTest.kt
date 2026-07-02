package kr.co.data.feature.user.mapper

import kr.co.data.feature.user.mapper.UserMapper.toUser
import kr.co.data.feature.user.mapper.UserMapper.toUserModel
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserMapperTest : BaseDataUnitTest() {
    @Test
    fun `maps user model to domain user`() {
        assertEquals(DataFixtures.user, DataFixtures.userModel.toUser())
    }

    @Test
    fun `maps domain user to user model`() {
        assertEquals(DataFixtures.userModel, DataFixtures.user.toUserModel())
    }
}
