package kr.co.presentation.feature.account.preview.provider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.domain.feature.profile.model.Gender
import kr.co.presentation.feature.account.viewmodel.SignUpScreenState
import java.time.LocalDate

internal class SignUpPreviewDataProvider : PreviewParameterProvider<SignUpScreenState> {

    override val values: Sequence<SignUpScreenState> = sequenceOf(
        SignUpScreenState(
            isSigningUp = false,
            email = "minary@gmail.com",
            password = "passwordValue",
            confirmPassword = "passwordValue",
            name = "minary",
            gender = Gender.NONE,
            birthday = LocalDate.now(),
            address = "서울시 강남구",
            phoneNumber = "010-0000-0000",
        ),
        SignUpScreenState(),
    )
}
