package kr.co.presentation.app.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.core.ui.common.load.LoadState
import kr.co.domain.service.remoteconfig.model.ServiceStatus
import kr.co.presentation.app.activity.MainActivityState
import kr.co.presentation.app.model.UserSessionUiModel

internal class MinaryAppPreviewParameterProvider :
    PreviewParameterProvider<MainActivityState> {

    override val values: Sequence<MainActivityState> = sequenceOf(
        MainActivityState(
            userSession = LoadState.Loading,
        ),
        MainActivityState(
            userSession = LoadState.Success(null),
        ),
        MainActivityState(
            userSession = LoadState.Success(
                UserSessionUiModel(email = "minary@gmail.com")
            ),
        ),
        MainActivityState(
            serviceStatus = ServiceStatus.Maintenance("점검 중입니다."),
        ),
    )
}
