package kr.co.presentation.main.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.presentation.common.state.LoadState


internal class LoadStatePreviewDataProvider : PreviewParameterProvider<LoadState<Unit>> {

    override val values: Sequence<LoadState<Unit>> = sequenceOf(
        LoadState.Uninitialized,
        LoadState.Loading,
        LoadState.Error(),
        LoadState.Success(Unit)
    )
}