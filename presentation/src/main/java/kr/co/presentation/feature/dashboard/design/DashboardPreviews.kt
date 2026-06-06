package kr.co.presentation.feature.dashboard.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import kr.co.presentation.feature.dashboard.model.DashboardUiModel
import kr.co.presentation.feature.dashboard.preview.DashboardPreviewDataProvider
import kr.co.presentation.feature.dashboard.screen.DashboardPreviewContent
import kr.co.presentation.design.ThemePreviews


@ThemePreviews
@Composable
fun DashboardScreenPreview(
    @PreviewParameter(DashboardPreviewDataProvider::class)
    dashboard: DashboardUiModel,
) {
    DashboardPreviewContent(dashboard)
}