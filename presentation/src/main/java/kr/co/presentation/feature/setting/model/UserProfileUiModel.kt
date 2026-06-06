package kr.co.presentation.feature.setting.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kr.co.domain.feature.profile.model.Gender
import kr.co.presentation.common.parceler.LocalDateParceler
import java.time.LocalDate


@Immutable
@Parcelize
@TypeParceler<LocalDate, LocalDateParceler>()
data class UserProfileUiModel(
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
) : Parcelable