package kr.co.data.feature.profile.mapper

import kr.co.core.common.model.Gender
import kr.co.core.datastore.proto.GenderProto
import kr.co.data.feature.profile.mapper.GenderMapper.toGender
import kr.co.data.feature.profile.mapper.UserProfileMapper.toUserProfile
import kr.co.data.feature.profile.mapper.UserProfileMapper.toUserProfileDto
import kr.co.data.feature.profile.mapper.UserProfileMapper.toUserProfileProto
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserProfileMapperTest : BaseDataUnitTest() {
    @Test
    fun `maps profile proto to domain profile`() {
        assertEquals(DataFixtures.userProfile, DataFixtures.userProfileProto.toUserProfile())
    }

    @Test
    fun `maps domain profile to profile proto`() {
        assertEquals(DataFixtures.userProfileProto, DataFixtures.userProfile.toUserProfileProto())
    }

    @Test
    fun `maps profile proto to dto`() {
        assertEquals(DataFixtures.userProfileDto, DataFixtures.userProfileProto.toUserProfileDto())
    }

    @Test
    fun `maps profile dto to proto`() {
        assertEquals(DataFixtures.userProfileProto, DataFixtures.userProfileDto.toUserProfileProto())
    }

    @Test
    fun `maps unknown profile dto gender to none`() {
        val dto = DataFixtures.userProfileDto.copy(gender = "unknown-value-test")

        val actual = dto.toUserProfileProto()

        assertEquals(GenderProto.NONE, actual.gender)
        assertEquals(Gender.NONE, actual.gender.toGender())
    }
}
