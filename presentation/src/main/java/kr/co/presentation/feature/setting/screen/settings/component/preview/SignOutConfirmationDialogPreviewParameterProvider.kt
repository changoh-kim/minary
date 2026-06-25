package kr.co.presentation.feature.setting.screen.settings.component.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.core.ui.common.text.UiText
import kr.co.presentation.R

internal class SignOutConfirmationDialogPreviewParameterProvider :
    PreviewParameterProvider<UiText> {

    override val values: Sequence<UiText> = sequenceOf(
        UiText.StringResource(R.string.settings_sign_out_confirm_message),
        UiText.StringResource(R.string.settings_logout_sync_dialog_message)
    )
}