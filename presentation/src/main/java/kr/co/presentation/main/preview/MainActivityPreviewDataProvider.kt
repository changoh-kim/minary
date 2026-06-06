package kr.co.presentation.main.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.presentation.common.state.LoadState
import kr.co.presentation.main.model.UserSessionUiModel

internal class MainActivityPreviewDataProvider :
    PreviewParameterProvider<LoadState<UserSessionUiModel>> {
    override val values: Sequence<LoadState<UserSessionUiModel>> = sequenceOf(
        /*LoadState.Uninitialized,
        LoadState.Loading,
        LoadState.Error(),*/
        LoadState.Success(
            UserSessionUiModel(email = "minary@gmail.com")
        )
    )
}