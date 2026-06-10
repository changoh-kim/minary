package kr.co.presentation.feature.diary.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kr.co.presentation.R
import kr.co.presentation.common.composable.LoadStateContent
import kr.co.presentation.common.composable.LoadingIconButton
import kr.co.presentation.common.extension.getString
import kr.co.presentation.design.ThemePreviews
import kr.co.presentation.feature.diary.composable.SkeletonDiaryContent
import kr.co.presentation.feature.diary.model.DiaryUiModel
import kr.co.presentation.feature.diary.preview.factory.DiaryPreviewDataFactory
import kr.co.presentation.feature.diary.preview.model.DiaryPreviewData
import kr.co.presentation.feature.diary.preview.provider.DiaryPreviewDataProvider
import kr.co.presentation.feature.diary.viewmodel.DiaryPreviewAction
import kr.co.presentation.feature.diary.viewmodel.DiaryPreviewSideEffect
import kr.co.presentation.feature.diary.viewmodel.DiaryPreviewViewModel
import kr.co.presentation.theme.MinaryTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.LocalDate

@Composable
fun DiaryPreviewScreen(
    onDiaryDeleted: () -> Unit = {},
    onLoadFailed: () -> Unit = {},
    onNavigateToEdit: (LocalDate, Boolean) -> Unit = { _, _ -> },
    viewModel: DiaryPreviewViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is DiaryPreviewSideEffect.DiaryDeleted -> onDiaryDeleted()
            is DiaryPreviewSideEffect.LoadFailed -> onLoadFailed()
            is DiaryPreviewSideEffect.NavigateToEdit -> onNavigateToEdit(sideEffect.date, sideEffect.isNewDiary)
            is DiaryPreviewSideEffect.ShowMessage -> coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
            }
        }
    }

    LoadStateContent(
        loadState = state.diaryLoadState,
        loading = {
            SkeletonDiaryContent()
        }
    ) { diary ->
        DiaryPreviewContent(
            diary = diary,
            isDeleting = state.isDeleting,
            snackbarHostState = snackbarHostState,
            onAction = viewModel::handleAction
        )
    }
}

@Composable
fun DiaryPreviewContent(
    diary: DiaryUiModel,
    isDeleting: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (DiaryPreviewAction) -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PreviewTopBar(isDeleting, onAction)
        }
    ) { paddingValues ->
        val textFieldColors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = diary.date.toString(),
                fontSize = 20.sp
            )

            TextField(
                value = diary.title,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                colors = textFieldColors,
                singleLine = true,
                maxLines = 1,
                label = { Text(stringResource(R.string.diary_title_hint)) },
                textStyle = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                )
            )

            TextField(
                value = diary.content,
                onValueChange = {},
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                readOnly = true,
                colors = textFieldColors,
                label = { Text(stringResource(R.string.diary_content_hint)) },
                textStyle = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                )
            )
        }
    }
}

@Composable
fun PreviewTopBar(
    isDeleting: Boolean = false,
    onAction: (DiaryPreviewAction) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoadingIconButton(
                onClick = { onAction(DiaryPreviewAction.DeleteClicked) },
                isLoading = isDeleting
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_delete),
                    contentDescription = stringResource(R.string.diary_delete_button_cd)
                )
            }

            Button(onClick = { onAction(DiaryPreviewAction.EditClicked) }) {
                Text(stringResource(R.string.diary_edit_button))
            }
        }
    }
}

@ThemePreviews
@Composable
private fun DiaryPreviewScreenPreview(
    @PreviewParameter(DiaryPreviewDataProvider::class)
    previewData: DiaryPreviewData,
) {
    val diary = DiaryPreviewDataFactory.createDiary(previewData)

    MinaryTheme {
        DiaryPreviewContent(diary)
    }
}
