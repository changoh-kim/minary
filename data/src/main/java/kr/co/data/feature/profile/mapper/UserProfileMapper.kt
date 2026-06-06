package kr.co.data.feature.profile.mapper

import kr.co.data.feature.profile.mapper.GenderMapper.toGender
import kr.co.data.feature.profile.mapper.GenderMapper.toGenderProto
import kr.co.data.feature.profile.model.UserProfileDto
import kr.co.data.proto.UserProfileProto
import kr.co.domain.common.extension.toGender
import kr.co.domain.common.extension.toLocalDate
import kr.co.domain.feature.profile.model.UserProfile
import java.time.LocalDate

object UserProfileMapper {
    fun UserProfileProto.toUserProfile(): UserProfile {
        return UserProfile(
            uid = uid,
            email = email,
            name = name,
            gender = gender.toGender(),
            birthday = birthday?.toLocalDate() ?: LocalDate.now(),
            address = address,
            phoneNumber = phoneNumber,
            nickname = nickname,
            profilePhotoUrl = profilePhotoUrl,
            joinedAt = joinedAt,
            lastModifiedAt = lastModifiedAt,
        )
    }

    fun UserProfile.toUserProfileProto(): UserProfileProto {
        return UserProfileProto.newBuilder()
            .setUid(uid)
            .setEmail(email)
            .setName(name)
            .setNickname(nickname)
            .setGender(gender.toGenderProto())
            .setBirthday(birthday.toString())
            .setAddress(address)
            .setPhoneNumber(phoneNumber)
            .setProfilePhotoUrl(profilePhotoUrl)
            .setJoinedAt(joinedAt)
            .setLastModifiedAt(lastModifiedAt)
            .build()
    }

    fun UserProfileProto.toUserProfileDto(): UserProfileDto {
        return UserProfileDto(
            uid = uid,
            email = email,
            name = name,
            gender = gender.name,
            birthday = birthday,
            address = address,
            phoneNumber = phoneNumber,
            nickname = nickname,
            profilePhotoUrl = profilePhotoUrl,
            joinedAt = joinedAt,
            lastModifiedAt = lastModifiedAt,
        )
    }

    fun UserProfileDto.toUserProfileProto(): UserProfileProto {
        return UserProfileProto.newBuilder()
            .setUid(uid)
            .setEmail(email)
            .setName(name)
            .setGender(gender.toGender().toGenderProto())
            .setBirthday(birthday)
            .setAddress(address)
            .setPhoneNumber(phoneNumber)
            .setNickname(nickname)
            .setProfilePhotoUrl(profilePhotoUrl)
            .setJoinedAt(joinedAt)
            .setLastModifiedAt(lastModifiedAt)
            .build()
    }
}