package kr.co.domain.feature.profile.model

import java.time.LocalDate


data class UserProfile(
    val uid: String = "",

    val email: String = "",
    val name: String = "",
    val gender: Gender = Gender.NONE,
    val birthday: LocalDate = LocalDate.now(),
    val address: String = "",
    val phoneNumber: String = "",

    val nickname: String = "",
    val profilePhotoUrl: String = "",

    val joinedAt: Long = 0L,
    val lastModifiedAt: Long = 0L,
)