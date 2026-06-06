package kr.co.presentation.feature.diary.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import kr.co.presentation.feature.diary.preview.model.DiaryPreviewData
import kr.co.presentation.feature.diary.preview.provider.DiaryPreviewDataProvider
import kr.co.presentation.feature.diary.screen.DiaryScreenPreviewContent
import kr.co.presentation.design.MinaryPreviews


@MinaryPreviews
@Composable
fun DiaryScreenPreview(
    @PreviewParameter(DiaryPreviewDataProvider::class)
    previewData: DiaryPreviewData,
) {
    DiaryScreenPreviewContent(previewData)
}