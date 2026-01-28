package kr.co.presentation.ui.screen.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kr.co.domain.model.emotion.Emotion
import kr.co.presentation.ui.extension.getString
import kr.co.presentation.ui.extension.color
import kr.co.presentation.ui.preview.DiaryPreviewFactory
import kr.co.presentation.ui.theme.MinaryTheme
import kr.co.presentation.viewmodel.diary.DiaryIntent
import kr.co.presentation.viewmodel.diary.DiarySideEffect
import kr.co.presentation.viewmodel.diary.DiaryState
import kr.co.presentation.viewmodel.diary.DiaryUiState
import kr.co.presentation.viewmodel.diary.DiaryViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect


@Composable
fun DiaryScreen(
    onNavigateToMainScreen: () -> Unit = {},
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val state: DiaryState by viewModel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is DiarySideEffect.NavigateToMainScreen -> onNavigateToMainScreen()
            is DiarySideEffect.ShowMsg -> coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
            }
        }
    }

    DiaryScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        intent = viewModel::handleIntent
    )
}

@Composable
private fun DiaryScreen(
    state: DiaryState = DiaryState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    intent: (DiaryIntent) -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            when (state.mode) {
                DiaryUiState.Edit -> EditTopBar(state.diary.emotion, intent)
                DiaryUiState.Preview -> PreviewTopBar(state.diary.emotion, intent)
            }
        }
    ) { paddingValues ->
        val enabled = (state.mode == DiaryUiState.Edit)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = state.diary.date.toString(),
                fontSize = 20.sp
            )
            TextField(
                value = state.diary.title,
                onValueChange = { intent(DiaryIntent.TitleChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                readOnly = !enabled,
                singleLine = true,
                maxLines = 1,
                label = { Text("Title") },
                textStyle = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                )
            )
            TextField(
                value = state.diary.content,
                onValueChange = { intent(DiaryIntent.ContentChanged(it)) },
                modifier = Modifier.fillMaxSize(),
                enabled = enabled,
                readOnly = !enabled,
                label = { Text("Content") },
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
    intent: (DiaryIntent) -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
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
            Button(onClick = { intent(DiaryIntent.DoneButtonClicked) }) {
                Text("Done")
            }
        }
    }


}

@Composable
fun PreviewTopBar(
    emotion: Emotion = Emotion.UNKNOWN,
    intent: (DiaryIntent) -> Unit = {}
) {
    Box (
        modifier = Modifier.fillMaxWidth().padding(16.dp),
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
            var isToggled by rememberSaveable { mutableStateOf(false) }

            IconButton(
                onClick = {
                    isToggled = !isToggled
                    intent(DiaryIntent.DeleteButtonClicked)
                }
            ) {
                Icon(
                    painter =
                        if (isToggled) painterResource(android.R.drawable.ic_menu_delete)
                        else painterResource(android.R.drawable.ic_menu_delete),
                    contentDescription = if (isToggled) "Selected icon button" else "Unselected icon button."
                )
            }

            Button(onClick = { intent(DiaryIntent.EditButtonClicked) }) {
                Text("Edit")
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
private fun PreviewDiaryScreenPreview() {
    val diary = DiaryPreviewFactory.createDiaryState(DiaryUiState.Preview)
    MinaryTheme {
        DiaryScreen(diary)
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun PreviewDiaryScreenPreview2() {
    val diary = DiaryPreviewFactory.createDiaryState(DiaryUiState.Edit)
    MinaryTheme {
        DiaryScreen(diary)
    }
}