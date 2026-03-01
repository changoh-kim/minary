package kr.co.presentation.feature.auth.preview.provider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.presentation.feature.auth.model.UserUiModel
import kr.co.presentation.feature.auth.viewmodel.LoginScreenState


internal class LoginPreviewDataProvider : PreviewParameterProvider<LoginScreenState> {

    override val values: Sequence<LoginScreenState> = sequenceOf(
        LoginScreenState(
            email = "minary@gmail.com",
            password = "passwordValue",
            isLoggingIn = false
        ),
        LoginScreenState(
            email = "minary@gmail.com",
            password = "passwordValue",
            isLoggingIn = true,
            loggedInUser = UserUiModel(name = "minary", email = "minary@gmail.com", photoUrl = "")
        ),
    )
}