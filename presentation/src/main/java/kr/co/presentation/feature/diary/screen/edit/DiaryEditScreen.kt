package kr.co.presentation.feature.diary.screen.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kr.co.core.ui.common.load.LoadStateContentContainer
import kr.co.core.ui.common.text.getString
import kr.co.core.ui.design.component.LoadingIconButton
import kr.co.core.ui.design.preview.ThemePreviews
import kr.co.core.ui.design.theme.MinaryTheme
import kr.co.presentation.R
import kr.co.presentation.feature.diary.screen.edit.component.AiAnalyzingOverlay
import kr.co.presentation.feature.diary.screen.detail.preview.DiaryDetailPreviewParameterProvider
import kr.co.presentation.feature.diary.model.DiaryUiModel
import kr.co.presentation.feature.diary.preview.DiaryPreviewDataFactory
import kr.co.presentation.feature.diary.preview.DiaryPreviewData
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.format.DateTimeFormatter

@Composable
fun DiaryEditScreen(
    onDiarySaved: () -> Unit = {},
    onLoadFailed: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: DiaryEditViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is DiaryEditSideEffect.DiarySaved -> onDiarySaved()
            is DiaryEditSideEffect.LoadFailed -> onLoadFailed()
            is DiaryEditSideEffect.ShowMessage -> coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
            }
            is DiaryEditSideEffect.MaxCharLimitReached -> {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    LoadStateContentContainer(
        loadState = state.diaryLoadState,
        loading = {
            DiaryEditSkeleton()
        }
    ) { diary ->
        Box(modifier = Modifier.fillMaxSize()) {
            DiaryEditContent(
                diary = diary,
                isSaving = state.isSaving,
                snackbarHostState = snackbarHostState,
                onAction = { action -> viewModel.handleAction(action) },
                onBack = onBack,
            )

            AiAnalyzingOverlay(visible = state.isAnalyzing)
        }
    }
}

@Composable
fun DiaryEditContent(
    diary: DiaryUiModel,
    isSaving: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (DiaryEditAction) -> Unit = {},
    onBack: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EditTopBar(isSaving, onAction, onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Entry Date Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.diary_entry_date_label),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.6.sp
                    )
                    Text(
                        text = diary.date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title Input
            val isTitleError = diary.title.length >= DiaryEditViewModel.MAX_TITLE_LENGTH
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = stringResource(R.string.diary_title_hint),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${diary.title.length}/${DiaryEditViewModel.MAX_TITLE_LENGTH}",
                    fontSize = 12.sp,
                    fontWeight = if (isTitleError) FontWeight.Bold else FontWeight.Normal,
                    color = if (isTitleError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextField(
                value = diary.title,
                onValueChange = { onAction(DiaryEditAction.TitleChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        if (isTitleError) 1.dp else 0.5.dp,
                        color = if (isTitleError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(12.dp)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Your Story Input
            val isContentError = diary.content.length >= DiaryEditViewModel.MAX_CONTENT_LENGTH
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = stringResource(R.string.diary_your_story_label),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${diary.content.length}/${DiaryEditViewModel.MAX_CONTENT_LENGTH}",
                    fontSize = 12.sp,
                    fontWeight = if (isContentError) FontWeight.Bold else FontWeight.Normal,
                    color = if (isContentError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextField(
                value = diary.content,
                onValueChange = { onAction(DiaryEditAction.ContentChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        if (isContentError) 1.dp else 0.5.dp,
                        color = if (isContentError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(12.dp)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 26.sp
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EditTopBar(
    isSaving: Boolean = false,
    onAction: (DiaryEditAction) -> Unit = {},
    onBack: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.profile_common_back_desc))
        }
        Text(
            text = stringResource(R.string.diary_edit_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
        LoadingIconButton(
            isLoading = isSaving,
            onClick = { onAction(DiaryEditAction.SaveClicked) },
            modifier = Modifier.padding(end = 4.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            iconContent = { isLoading ->
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.diary_save_button),
                    tint = if (isLoading) Color.Transparent else MaterialTheme.colorScheme.onPrimary
                )
            }
        )
    }
}

@ThemePreviews
@Composable
private fun DiaryEditScreenPreview(
    @PreviewParameter(DiaryDetailPreviewParameterProvider::class)
    previewData: DiaryPreviewData,
) {
    val diary = DiaryPreviewDataFactory.createDiary(previewData)

    MinaryTheme {
        DiaryEditContent(diary)
    }
}
