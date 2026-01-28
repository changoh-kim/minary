package kr.co.presentation.viewmodel.diary

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.domain.exception.DiaryNotFoundException
import kr.co.domain.model.emotion.Emotion
import kr.co.domain.usecase.diary.DeleteDiaryUseCase
import kr.co.domain.usecase.diary.GetDiaryUseCase
import kr.co.domain.usecase.diary.UpsertDiaryUseCase
import kr.co.presentation.R
import kr.co.presentation.mapper.UiDiaryMapper.toDiaryData
import kr.co.presentation.mapper.UiDiaryMapper.toUiDiary
import kr.co.presentation.ui.model.common.UiDiary
import kr.co.presentation.ui.model.common.UiText
import kr.co.presentation.ui.navigation.route.Diary
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.blockingIntent
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import javax.inject.Inject


@Immutable
sealed interface DiaryUiState {
    object Edit : DiaryUiState
    object Preview : DiaryUiState
}

@Immutable
data class DiaryState(
    val mode: DiaryUiState = DiaryUiState.Preview,
    val diary: UiDiary = UiDiary()
)

@Immutable
sealed interface DiarySideEffect {
    object NavigateToMainScreen : DiarySideEffect
    data class ShowMsg(val uiText: UiText) : DiarySideEffect
}

sealed interface DiaryIntent {
    data class TitleChanged(val newTitle: String) : DiaryIntent
    data class ContentChanged(val newContent: String) : DiaryIntent
    object DeleteButtonClicked : DiaryIntent
    object EditButtonClicked : DiaryIntent
    object DoneButtonClicked : DiaryIntent
}

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getDiaryUseCase: GetDiaryUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase,
    private val upsertDiaryUseCase: UpsertDiaryUseCase,
) : ViewModel(), ContainerHost<DiaryState, DiarySideEffect> {

    companion object {
        private const val KEY_DIARY_ID = "diary_id"
        private const val KEY_TITLE = "title"
        private const val KEY_CONTENT = "content"
        private const val KEY_EMOTION = "emotion"
    }

    override val container =
        container<DiaryState, DiarySideEffect>(DiaryState())

    init {
         initializeState()
    }

    private fun initializeState() = intent {
        val route = savedStateHandle.toRoute<Diary>()
        val date = LocalDate.of(route.year, route.month, route.date)

        getDiaryUseCase(date).onSuccess { diary ->
            val uiDiary = diary.toUiDiary()
            reduce {
                state.copy(
                    mode = DiaryUiState.Preview,
                    diary = uiDiary,
                )
            }
            savedStateHandle[KEY_DIARY_ID] = uiDiary.id
            savedStateHandle[KEY_TITLE] = uiDiary.title
            savedStateHandle[KEY_CONTENT] = uiDiary.content
            savedStateHandle[KEY_EMOTION] = uiDiary.emotion
        }.onFailure { error ->
            when (error) {
                is DiaryNotFoundException -> {
                    val diaryId = savedStateHandle.get<Long>(KEY_DIARY_ID) ?: 0L
                    val title = savedStateHandle.get<String>(KEY_TITLE) ?: ""
                    val content = savedStateHandle.get<String>(KEY_CONTENT) ?: ""
                    val emotion = savedStateHandle.get<Emotion>(KEY_EMOTION) ?: Emotion.UNKNOWN

                    reduce {
                        state.copy(
                            mode = DiaryUiState.Edit,
                            diary = UiDiary(
                                id = diaryId,
                                date = date,
                                title = title,
                                content = content,
                                emotion = emotion,
                            )
                        )
                    }
                }

                else -> postSideEffect(DiarySideEffect.ShowMsg(handleErrorMsg(error)))
            }
        }
    }

    fun handleIntent(intent: DiaryIntent) {
        when (intent) {
            is DiaryIntent.TitleChanged -> updateTitle(intent.newTitle)
            is DiaryIntent.ContentChanged -> updateContent(intent.newContent)
            is DiaryIntent.DeleteButtonClicked -> deleteDiary()
            is DiaryIntent.EditButtonClicked -> changeMode(DiaryUiState.Edit)
            is DiaryIntent.DoneButtonClicked -> updateDiary()
        }
    }

    private fun updateTitle(newTitle: String) = blockingIntent {
        reduce { state.copy(diary = state.diary.copy(title = newTitle)) }
        savedStateHandle[KEY_TITLE] = newTitle
    }

    private fun updateContent(newContent: String) = blockingIntent {
        reduce { state.copy(diary = state.diary.copy(content = newContent)) }
        savedStateHandle[KEY_CONTENT] = newContent
    }

    private fun deleteDiary() = intent {
        deleteDiaryUseCase(
            state.diary.toDiaryData()
        ).onSuccess {
            postSideEffect(DiarySideEffect.NavigateToMainScreen)
        }.onFailure { error ->
            postSideEffect(DiarySideEffect.ShowMsg(handleErrorMsg(error)))
        }
    }

    private fun changeMode(mode: DiaryUiState) = intent {
        reduce { state.copy(mode = mode) }
    }

    private fun updateDiary() = intent {
        if (state.diary.title.isNullOrBlank()) {
            postSideEffect(DiarySideEffect.ShowMsg(UiText.StringResource(R.string.diary_title_is_empty)))
            return@intent
        }

        if (state.diary.content.isNullOrBlank()) {
            postSideEffect(DiarySideEffect.ShowMsg(UiText.StringResource(R.string.diary_content_is_empty)))
            return@intent
        }

        upsertDiaryUseCase(
            state.diary.toDiaryData()
        ).onSuccess { resultData ->
            reduce {
                state.copy(
                    mode = DiaryUiState.Preview,
                    diary = state.diary.copy(
                        id = resultData.id,
                        emotion = resultData.emotion,
                    )
                )
            }
            savedStateHandle[KEY_DIARY_ID] = resultData.id
            savedStateHandle[KEY_EMOTION] = resultData.emotion
        }.onFailure { error ->
            postSideEffect(DiarySideEffect.ShowMsg(handleErrorMsg(error)))
        }
    }

    private fun handleErrorMsg(error: Throwable): UiText {
        return error.message
            .takeIf { !it.isNullOrBlank() }
            ?.let { UiText.DynamicString(it) }
            ?: UiText.StringResource(R.string.unknown_error)
    }
}