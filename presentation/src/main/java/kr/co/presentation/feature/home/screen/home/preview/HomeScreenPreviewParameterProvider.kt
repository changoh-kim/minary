package kr.co.presentation.feature.home.screen.home.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.core.common.error.DomainError
import kr.co.core.common.state.SyncProcessState
import kr.co.presentation.feature.home.screen.home.HomeScreenState

internal class HomeScreenPreviewParameterProvider :
    PreviewParameterProvider<HomeScreenState> {

    override val values: Sequence<HomeScreenState> = sequenceOf(
        HomeScreenState(
            initDiarySyncProcessState = SyncProcessState.Idle
        ),
        HomeScreenState(
            initDiarySyncProcessState = SyncProcessState.InProgress.Determinate(0.5f)
        ),
        HomeScreenState(
            initDiarySyncProcessState = SyncProcessState.Failed(DomainError.Timeout)
        ),
    )
}