package kr.co.presentation.feature.home.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.domain.common.state.SyncProcessState
import kr.co.domain.error.DomainError
import kr.co.presentation.feature.home.viewmodel.HomeScreenState


internal class HomeScreenPreviewDataProvider : PreviewParameterProvider<HomeScreenState> {

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