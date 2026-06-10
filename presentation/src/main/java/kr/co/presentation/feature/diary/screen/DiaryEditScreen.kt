package kr.co.presentation.feature.diary.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
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
import kr.co.presentation.common.composable.LoadingButton
import kr.co.presentation.common.extension.getString
import kr.co.presentation.design.ThemePreviews
import kr.co.presentation.feature.diary.composable.SkeletonDiaryContent
import kr.co.presentation.feature.diary.model.DiaryUiModel
import kr.co.presentation.feature.diary.preview.factory.DiaryPreviewDataFactory
import kr.co.presentation.feature.diary.preview.model.DiaryPreviewData
import kr.co.presentation.feature.diary.preview.provider.DiaryPreviewDataProvider
import kr.co.presentation.feature.diary.viewmodel.DiaryEditAction
import kr.co.presentation.feature.diary.viewmodel.DiaryEditSideEffect
import kr.co.presentation.feature.diary.viewmodel.DiaryEditViewModel
import kr.co.presentation.theme.MinaryTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun DiaryEditScreen(
    onDiarySaved: () -> Unit = {},
    onLoadFailed: () -> Unit = {},
    viewModel: DiaryEditViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is DiaryEditSideEffect.DiarySaved -> onDiarySaved()
            is DiaryEditSideEffect.LoadFailed -> onLoadFailed()
            is DiaryEditSideEffect.ShowMessage -> coroutineScope.launch {
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
        DiaryEditContent(
            diary = diary,
            isSaving = state.isSaving,
            snackbarHostState = snackbarHostState,
            onAction = viewModel::handleAction
        )
    }
}

@Composable
fun DiaryEditContent(
    diary: DiaryUiModel,
    isSaving: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (DiaryEditAction) -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EditTopBar(isSaving, onAction)
        }
    ) { paddingValues ->
        val textFieldColors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                onValueChange = { onAction(DiaryEditAction.TitleChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
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
                onValueChange = { onAction(DiaryEditAction.ContentChanged(it)) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
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
fun EditTopBar(
    isSaving: Boolean = false,
    onAction: (DiaryEditAction) -> Unit = {}
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
            LoadingButton(
                text = stringResource(R.string.diary_save_button),
                isLoading = isSaving,
                onClick = { onAction(DiaryEditAction.SaveClicked) },
            )
        }
    }
}

@ThemePreviews
@Composable
private fun DiaryEditScreenPreview(
    @PreviewParameter(DiaryPreviewDataProvider::class)
    previewData: DiaryPreviewData,
) {
    val diary = DiaryPreviewDataFactory.createDiary(previewData)

    MinaryTheme {
        DiaryEditContent(diary)
    }
}
