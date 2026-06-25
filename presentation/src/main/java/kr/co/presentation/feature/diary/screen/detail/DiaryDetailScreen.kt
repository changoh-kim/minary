package kr.co.presentation.feature.diary.screen.detail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kr.co.core.common.model.Emotion
import kr.co.core.ui.common.emotion.color
import kr.co.core.ui.common.emotion.resId
import kr.co.core.ui.common.load.LoadStateContentContainer
import kr.co.core.ui.common.text.getString
import kr.co.core.ui.design.preview.ThemePreviews
import kr.co.core.ui.design.theme.MinaryTheme
import kr.co.presentation.R
import kr.co.presentation.feature.diary.screen.detail.component.DiaryDeleteDialog
import kr.co.presentation.feature.diary.screen.detail.preview.DiaryDetailPreviewParameterProvider
import kr.co.presentation.feature.diary.model.DiaryUiModel
import kr.co.presentation.feature.diary.preview.DiaryPreviewData
import kr.co.presentation.feature.diary.preview.DiaryPreviewDataFactory
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DiaryDetailScreen(
    onDiaryDeleted: () -> Unit = {},
    onLoadFailed: () -> Unit = {},
    onNavigateToEdit: (LocalDate, Boolean) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    viewModel: DiaryDetailViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is DiaryDetailSideEffect.DiaryDeleted -> onDiaryDeleted()
            is DiaryDetailSideEffect.LoadFailed -> onLoadFailed()
            is DiaryDetailSideEffect.NavigateToEdit -> onNavigateToEdit(sideEffect.date, sideEffect.isNewDiary)
            is DiaryDetailSideEffect.ShowMessage -> coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
            }
        }
    }

    if (state.showDeleteDialog) {
        DiaryDeleteDialog(
            onConfirm = { viewModel.handleAction(DiaryDetailAction.DeleteConfirmed) },
            onDismiss = { viewModel.handleAction(DiaryDetailAction.DeleteCancelled) }
        )
    }

    LoadStateContentContainer(
        loadState = state.diaryLoadState,
        loading = {
            DiaryDetailSkeleton()
        }
    ) { diary ->
        DiaryDetailContent(
            diary = diary,
            onAction = { action -> viewModel.handleAction(action) },
            onBack = onBack,
            snackbarHostState = snackbarHostState
        )
    }
}

@Composable
fun DiaryDetailContent(
    diary: DiaryUiModel,
    onAction: (DiaryDetailAction) -> Unit = {},
    onBack: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DetailTopBar(diary.date, onAction, onBack)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAction(DiaryDetailAction.EditClicked) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.diary_edit_button))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Diary Image (if available)
            if (diary.imageUrls.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    AsyncImage(
                        model = diary.imageUrls.first(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                // Placeholder or gap as per design
                Spacer(modifier = Modifier.height(16.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                /* TODO: 추후 location 기능 추가시 활용
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "San Francisco, CA",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))*/

                // Title
                Text(
                    text = diary.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Content
                Text(
                    text = diary.content,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Mood Section
                Text(
                    text = stringResource(R.string.diary_emotion_label),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 0.6.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(diary.emotions) { emotion ->
                        EmotionChip(emotion)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun EmotionChip(emotion: Emotion) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(9999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(15.dp)
                .background(emotion.color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(emotion.resId),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
fun DetailTopBar(
    date: LocalDate,
    onAction: (DiaryDetailAction) -> Unit,
    onBack: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

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

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEEE")),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.profile_common_more_desc)
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.diary_delete_menu_button),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        showMenu = false
                        onAction(DiaryDetailAction.DeleteClicked)
                    },
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun DiaryDetailScreenPreview(
    @PreviewParameter(DiaryDetailPreviewParameterProvider::class)
    previewData: DiaryPreviewData,
) {
    val diary = DiaryPreviewDataFactory.createDiary(previewData)

    MinaryTheme {
        DiaryDetailContent(diary)
    }
}
