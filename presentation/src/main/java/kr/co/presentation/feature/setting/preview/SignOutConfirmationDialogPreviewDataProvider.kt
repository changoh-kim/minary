package kr.co.presentation.feature.setting.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.presentation.R
import kr.co.presentation.common.model.UiText

internal class SignOutConfirmationDialogPreviewDataProvider : PreviewParameterProvider<UiText> {
    override val values: Sequence<UiText> = sequenceOf(
        UiText.StringResource(R.string.settings_sign_out_confirm_message),
        UiText.StringResource(R.string.settings_logout_sync_dialog_message)
    )
}