package kr.co.data.feature.profile.mapper

import kr.co.core.common.model.Gender
import kr.co.core.datastore.proto.GenderProto
import kr.co.data.feature.profile.mapper.GenderMapper.toGender
import kr.co.data.feature.profile.mapper.GenderMapper.toGenderProto
import kr.co.data.testing.BaseDataUnitTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GenderMapperTest : BaseDataUnitTest() {
    @Test
    fun `maps domain gender to proto gender`() {
        assertEquals(GenderProto.NONE, Gender.NONE.toGenderProto())
        assertEquals(GenderProto.MALE, Gender.MALE.toGenderProto())
        assertEquals(GenderProto.FEMALE, Gender.FEMALE.toGenderProto())
        assertEquals(GenderProto.OTHER, Gender.OTHER.toGenderProto())
    }

    @Test
    fun `maps proto gender to domain gender`() {
        assertEquals(Gender.NONE, GenderProto.NONE.toGender())
        assertEquals(Gender.MALE, GenderProto.MALE.toGender())
        assertEquals(Gender.FEMALE, GenderProto.FEMALE.toGender())
        assertEquals(Gender.OTHER, GenderProto.OTHER.toGender())
        assertEquals(Gender.NONE, GenderProto.UNRECOGNIZED.toGender())
    }
}
