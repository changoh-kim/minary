package kr.co.presentation.feature.diary.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kr.co.domain.feature.emotion.Emotion
import kr.co.presentation.R
import kr.co.presentation.common.composable.LoadStateContent
import kr.co.presentation.common.composable.LoadingButton
import kr.co.presentation.common.composable.LoadingIconButton
import kr.co.presentation.common.extension.color
import kr.co.presentation.common.extension.getString
import kr.co.presentation.feature.diary.composable.SkeletonDiaryContent
import kr.co.presentation.feature.diary.model.DiaryUiModel
import kr.co.presentation.feature.diary.preview.factory.DiaryPreviewDataFactory
import kr.co.presentation.feature.diary.preview.model.DiaryPreviewData
import kr.co.presentation.feature.diary.preview.provider.DiaryPreviewDataProvider
import kr.co.presentation.feature.diary.viewmodel.DiaryAction
import kr.co.presentation.feature.diary.viewmodel.DiaryScreenMode
import kr.co.presentation.feature.diary.viewmodel.DiarySideEffect
import kr.co.presentation.feature.diary.viewmodel.DiaryViewModel
import kr.co.presentation.theme.MinaryTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect


@Composable
fun DiaryScreen(
    onDiaryDeleted: () -> Unit = {},
    onLoadFailed: () -> Unit = {},
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is DiarySideEffect.DiaryDeleted -> onDiaryDeleted()
            is DiarySideEffect.LoadFailed -> onLoadFailed()
            is DiarySideEffect.ShowMessage -> coroutineScope.launch {
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
        DiaryContent(
            diary = diary,
            screenMode = state.screenMode,
            isSaving = state.isSaving,
            isDeleting = state.isDeleting,
            snackbarHostState = snackbarHostState,
            onAction = viewModel::handleAction
        )
    }
}

@Composable
fun DiaryContent(
    diary: DiaryUiModel,
    screenMode: DiaryScreenMode = DiaryScreenMode.Edit,
    isSaving: Boolean = false,
    isDeleting: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (DiaryAction) -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            when (screenMode) {
                DiaryScreenMode.Edit -> EditTopBar(diary.emotion, isSaving, onAction)
                DiaryScreenMode.Preview -> PreviewTopBar(diary.emotion, isDeleting, onAction)
            }
        }
    ) { paddingValues ->
        val readOnly = (screenMode == DiaryScreenMode.Preview)

        val textFieldColors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = if (readOnly) Color.Transparent else MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = if (readOnly) Color.Transparent else MaterialTheme.colorScheme.onSurfaceVariant
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
                onValueChange = { onAction(DiaryAction.TitleChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = readOnly,
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
                onValueChange = { onAction(DiaryAction.ContentChanged(it)) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                readOnly = readOnly,
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
    emotion: Emotion = Emotion.UNKNOWN,
    isSaving: Boolean = false,
    onAction: (DiaryAction) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        EmotionIcon(
            modifier = Modifier.align(Alignment.CenterStart),
            emotion
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoadingButton(
                text = stringResource(R.string.diary_save_button),
                isLoading = isSaving,
                onClick = { onAction(DiaryAction.SaveClicked) },
            )
        }
    }
}

@Composable
fun PreviewTopBar(
    emotion: Emotion = Emotion.UNKNOWN,
    isDeleting: Boolean = false,
    onAction: (DiaryAction) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        EmotionIcon(
            modifier = Modifier.align(Alignment.CenterStart),
            emotion
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoadingIconButton(
                onClick = { onAction(DiaryAction.DeleteClicked) },
                isLoading = isDeleting
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_delete),
                    contentDescription = stringResource(R.string.diary_delete_button_cd)
                )
            }

            Button(onClick = { onAction(DiaryAction.EditClicked) }) {
                Text(stringResource(R.string.diary_edit_button))
            }
        }
    }
}

@Composable
fun EmotionIcon(
    modifier: Modifier = Modifier,
    emotion: Emotion
) {
    if (emotion != Emotion.UNKNOWN) {
        Box(
            modifier = modifier
                .size(24.dp)
                .background(emotion.color, CircleShape)
        )
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun DiaryContentPreview(
    @PreviewParameter(DiaryPreviewDataProvider::class) diaryPreviewData: DiaryPreviewData,
) {
    val diary = DiaryPreviewDataFactory.createDiary(diaryPreviewData.emotion)

    MinaryTheme {
        DiaryContent(diary, diaryPreviewData.screenMode)
    }
}