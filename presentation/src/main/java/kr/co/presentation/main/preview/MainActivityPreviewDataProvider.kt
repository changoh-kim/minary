package kr.co.presentation.main.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.presentation.common.state.LoadState
import kr.co.presentation.feature.auth.model.UserUiModel


internal class MainActivityPreviewDataProvider : PreviewParameterProvider<LoadState<UserUiModel>> {

    override val values: Sequence<LoadState<UserUiModel>> = sequenceOf(
        LoadState.Uninitialized,
        LoadState.Loading,
        LoadState.Error(),
        LoadState.Success(
            UserUiModel(
                name = "minary",
                email = "minary@gmail.com",
                photoUrl = ""
            )
        )
    )
}