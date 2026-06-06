package kr.co.data.feature.profile.mapper

import kr.co.data.proto.GenderProto
import kr.co.domain.feature.profile.model.Gender

object GenderMapper {
    fun Gender.toGenderProto(): GenderProto {
        return when (this) {
            Gender.OTHER -> GenderProto.OTHER
            Gender.MALE -> GenderProto.MALE
            Gender.FEMALE -> GenderProto.FEMALE
            Gender.NONE -> GenderProto.NONE
        }
    }

    fun GenderProto.toGender(): Gender {
        return when (this) {
            GenderProto.OTHER -> Gender.OTHER
            GenderProto.MALE -> Gender.MALE
            GenderProto.FEMALE -> Gender.FEMALE
            else -> Gender.NONE
        }
    }
}