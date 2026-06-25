package kr.co.presentation.feature.diary.screen.detail.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kr.co.core.ui.design.preview.ThemePreviews
import kr.co.core.ui.design.theme.MinaryTheme
import kr.co.presentation.R

@Composable
fun DiaryDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color(0xFFBA1A1A)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.diary_delete_dialog_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.diary_delete_dialog_message),
                fontSize = 16.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.dialog_confirm),
                    color = androidx.compose.ui.graphics.Color(0xFFBA1A1A),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.dialog_cancel),
                    color = androidx.compose.ui.graphics.Color(0xFF1978E5),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

@ThemePreviews
@Composable
private fun DiaryDeleteDialogPreview() {
    MinaryTheme {
        DiaryDeleteDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}