package kr.co.data.feature.profile.model

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kr.co.data.proto.GenderProto

@Keep
@Serializable
data class UserProfileDto(

    @SerialName(UID)
    @get:PropertyName(UID)
    @PropertyName(UID)
    val uid: String = "",

    @SerialName(EMAIL)
    @get:PropertyName(EMAIL)
    @PropertyName(EMAIL)
    val email: String = "",

    @SerialName(NAME)
    @get:PropertyName(NAME)
    @PropertyName(NAME)
    val name: String = "",

    @SerialName(GENDER)
    @get:PropertyName(GENDER)
    @PropertyName(GENDER)
    val gender: String = GenderProto.NONE.name,

    @SerialName(BIRTHDAY)
    @get:PropertyName(BIRTHDAY)
    @PropertyName(BIRTHDAY)
    val birthday: String = "",

    @SerialName(ADDRESS)
    @get:PropertyName(ADDRESS)
    @PropertyName(ADDRESS)
    val address: String = "",

    @SerialName(PHONE_NUMBER)
    @get:PropertyName(PHONE_NUMBER)
    @PropertyName(PHONE_NUMBER)
    val phoneNumber: String = "",

    @SerialName(NICKNAME)
    @get:PropertyName(NICKNAME)
    @PropertyName(NICKNAME)
    val nickname: String = "",

    @SerialName(PROFILE_PHOTO_URL)
    @get:PropertyName(PROFILE_PHOTO_URL)
    @PropertyName(PROFILE_PHOTO_URL)
    val profilePhotoUrl: String = "",

    @SerialName(JOINED_AT)
    @get:PropertyName(JOINED_AT)
    @PropertyName(JOINED_AT)
    val joinedAt: Long = System.currentTimeMillis(),

    @SerialName(LAST_MODIFIED_AT)
    @get:PropertyName(LAST_MODIFIED_AT)
    @PropertyName(LAST_MODIFIED_AT)
    val lastModifiedAt: Long = System.currentTimeMillis(),
) {
    constructor() : this(
        uid = "",
        email = "",
        name = "",
        gender = GenderProto.NONE.name,
        birthday = "",
        address = "",
        phoneNumber = "",
        nickname = "",
        profilePhotoUrl = "",
        joinedAt = System.currentTimeMillis(),
        lastModifiedAt = System.currentTimeMillis(),
    )

    companion object {
        const val UID = "uid"
        const val EMAIL = "email"
        const val NAME = "name"
        const val GENDER = "gender"
        const val BIRTHDAY = "birthday"
        const val ADDRESS = "address"
        const val PHONE_NUMBER = "phoneNumber"
        const val NICKNAME = "nickname"
        const val PROFILE_PHOTO_URL = "profilePhotoUrl"
        const val JOINED_AT = "joinedAt"
        const val LAST_MODIFIED_AT = "lastModifiedAt"
    }
}