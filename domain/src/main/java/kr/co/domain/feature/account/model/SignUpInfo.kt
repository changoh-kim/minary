package kr.co.domain.feature.account.model

import kr.co.domain.feature.profile.model.Gender
import java.time.LocalDate

data class SignUpInfo(
    val email: String,
    val password: String,
    val name: String,
    val gender: Gender,
    val birthday: LocalDate,
    val address: String,
    val phoneNumber: String,
)