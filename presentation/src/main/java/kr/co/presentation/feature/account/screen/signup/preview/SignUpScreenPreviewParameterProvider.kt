package kr.co.presentation.feature.account.screen.signup.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.core.common.model.Gender
import kr.co.presentation.feature.account.screen.signup.SignUpScreenState
import java.time.LocalDate

internal class SignUpScreenPreviewParameterProvider : PreviewParameterProvider<SignUpScreenState> {

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
