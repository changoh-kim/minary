package kr.co.presentation.feature.diary.viewmodel

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import kr.co.domain.feature.diary.exception.DiaryNotFoundException
import kr.co.domain.feature.diary.usecase.DeleteDiaryUseCase
import kr.co.domain.feature.diary.usecase.GetDiaryUseCase
import kr.co.domain.feature.diary.usecase.UpsertDiaryUseCase
import kr.co.domain.feature.emotion.Emotion
import kr.co.presentation.R
import kr.co.presentation.common.model.UiText
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiary
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiaryUiModel
import kr.co.presentation.feature.diary.model.DiaryUiModel
import kr.co.presentation.feature.diary.navigation.DiaryRoute
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.blockingIntent
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import javax.inject.Inject


@Immutable
@Parcelize
sealed interface DiaryScreenMode : Parcelable {
    @Parcelize object Edit : DiaryScreenMode
    @Parcelize object Preview : DiaryScreenMode
}

@Immutable
data class DiaryUiState(
    val screenMode: DiaryScreenMode = DiaryScreenMode.Preview,
    val diaryUiModel: DiaryUiModel = DiaryUiModel()
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
) : ViewModel(), ContainerHost<DiaryUiState, DiarySideEffect> {

    companion object {
        private const val KEY_SCREEN_MODE = "screen_mode"
        private const val KEY_DIARY_ID = "diary_id"
        private const val KEY_PASSWORD = "title"
        private const val KEY_CONTENT = "content"
        private const val KEY_EMOTION = "emotion"
    }

    override val container =
        container<DiaryUiState, DiarySideEffect>(DiaryUiState())

    init {
        initializeState()
    }

    private fun initializeState() = intent {
        val route = savedStateHandle.toRoute<DiaryRoute>()
        val date = LocalDate.of(route.year, route.month, route.date)

        val screenMode = savedStateHandle.get<DiaryScreenMode>(KEY_SCREEN_MODE) ?: DiaryScreenMode.Edit
        val diaryId = savedStateHandle.get<Long>(KEY_DIARY_ID) ?: 0L
        val title = savedStateHandle.get<String>(KEY_PASSWORD) ?: ""
        val content = savedStateHandle.get<String>(KEY_CONTENT) ?: ""
        val emotion = savedStateHandle.get<Emotion>(KEY_EMOTION) ?: Emotion.UNKNOWN

        getDiaryUseCase(date).onSuccess { diary ->
            val diaryUiModel = diary.toDiaryUiModel()
            val screenMode = DiaryScreenMode.Preview
            reduce {
                state.copy(
                    screenMode = screenMode,
                    diaryUiModel = diaryUiModel,
                )
            }
            savedStateHandle[KEY_SCREEN_MODE] = screenMode
            savedStateHandle[KEY_DIARY_ID] = diaryUiModel.id
            savedStateHandle[KEY_PASSWORD] = diaryUiModel.title
            savedStateHandle[KEY_CONTENT] = diaryUiModel.content
            savedStateHandle[KEY_EMOTION] = diaryUiModel.emotion
        }.onFailure { error ->
            when (error) {
                is DiaryNotFoundException -> {
                    reduce {
                        state.copy(
                            screenMode = screenMode,
                            diaryUiModel = DiaryUiModel(
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
            is DiaryIntent.EditButtonClicked -> changeScreenMode(DiaryScreenMode.Edit)
            is DiaryIntent.DoneButtonClicked -> updateDiary()
        }
    }

    private fun updateTitle(newTitle: String) = blockingIntent {
        reduce { state.copy(diaryUiModel = state.diaryUiModel.copy(title = newTitle)) }
        savedStateHandle[KEY_PASSWORD] = newTitle
    }

    private fun updateContent(newContent: String) = blockingIntent {
        reduce { state.copy(diaryUiModel = state.diaryUiModel.copy(content = newContent)) }
        savedStateHandle[KEY_CONTENT] = newContent
    }

    private fun deleteDiary() = intent {
        deleteDiaryUseCase(
            state.diaryUiModel.toDiary()
        ).onSuccess {
            postSideEffect(DiarySideEffect.NavigateToMainScreen)
        }.onFailure { error ->
            postSideEffect(DiarySideEffect.ShowMsg(handleErrorMsg(error)))
        }
    }

    private fun changeScreenMode(screenMode: DiaryScreenMode) = intent {
        reduce { state.copy(screenMode = screenMode) }
        savedStateHandle[KEY_SCREEN_MODE] = screenMode
    }

    private fun updateDiary() = intent {
        if (state.diaryUiModel.title.isNullOrBlank()) {
            postSideEffect(DiarySideEffect.ShowMsg(UiText.StringResource(R.string.diary_title_is_empty)))
            return@intent
        }

        if (state.diaryUiModel.content.isNullOrBlank()) {
            postSideEffect(DiarySideEffect.ShowMsg(UiText.StringResource(R.string.diary_content_is_empty)))
            return@intent
        }

        upsertDiaryUseCase(
            state.diaryUiModel.toDiary()
        ).onSuccess { diary ->
            val diaryUiModel = diary.toDiaryUiModel()
            val screenMode = DiaryScreenMode.Preview
            reduce {
                state.copy(
                    screenMode = screenMode,
                    diaryUiModel = diaryUiModel
                )
            }
            savedStateHandle[KEY_SCREEN_MODE] = screenMode
            savedStateHandle[KEY_DIARY_ID] = diaryUiModel.id
            savedStateHandle[KEY_PASSWORD] = diaryUiModel.title
            savedStateHandle[KEY_CONTENT] = diaryUiModel.content
            savedStateHandle[KEY_EMOTION] = diaryUiModel.emotion
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