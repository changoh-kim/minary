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
import kr.co.presentation.common.extension.getLocalDate
import kr.co.presentation.common.extension.getLong
import kr.co.presentation.common.extension.safeCall
import kr.co.presentation.common.model.UiText
import kr.co.presentation.common.state.LoadState
import kr.co.presentation.common.state.data
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiary
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiaryUiModel
import kr.co.presentation.feature.diary.model.DiaryUiModel
import kr.co.presentation.feature.diary.navigation.DiaryRoute
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import javax.inject.Inject


@Immutable
@Parcelize
sealed interface DiaryScreenMode : Parcelable {
    @Parcelize
    object Edit : DiaryScreenMode

    @Parcelize
    object Preview : DiaryScreenMode
}

@Immutable
data class DiaryScreenState(
    val screenMode: DiaryScreenMode = DiaryScreenMode.Edit,
    val diaryLoadState: LoadState<DiaryUiModel> = LoadState.Uninitialized,
    val isDoneBtnLoading: Boolean = false,
    val isDeleteBtnLoading: Boolean = false,
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
) : ViewModel(), ContainerHost<DiaryScreenState, DiarySideEffect> {

    private companion object {
        private const val KEY_ID = "id"
        private const val KEY_DATE = "date"
        private const val KEY_TITLE = "title"
        private const val KEY_CONTENT = "content"
        private const val KEY_EMOTION = "emotion"
    }

    override val container =
        container<DiaryScreenState, DiarySideEffect>(DiaryScreenState())

    init {
        loadDiary()
    }

    private fun loadDiary() = intent {
        val route = savedStateHandle.toRoute<DiaryRoute>()
        val targetDate = LocalDate.of(route.year, route.month, route.date)

        val saveStateDiaryId = savedStateHandle.getLong(KEY_ID, 0L)
        val saveStateDiaryDate = savedStateHandle.getLocalDate(KEY_DATE, targetDate)
        val saveStateDiaryTitle = savedStateHandle[KEY_TITLE] ?: ""
        val saveStateDiaryContent = savedStateHandle[KEY_CONTENT] ?: ""
        val saveStateDiaryEmotion = savedStateHandle[KEY_EMOTION] ?: Emotion.UNKNOWN

        if (saveStateDiaryTitle.isNotBlank() || saveStateDiaryContent.isNotBlank()) {
            // 작성중인 일기 내용이 있었다면
            setEditState(
                DiaryUiModel(
                    id = saveStateDiaryId,
                    date = saveStateDiaryDate,
                    title = saveStateDiaryTitle,
                    content = saveStateDiaryContent,
                    emotion = saveStateDiaryEmotion,
                )
            )
        } else {
            // 작성중인 일기 내용이 없다면, DB에서 가져오기 실행
            safeCall { getDiaryUseCase(targetDate) }
                .map { it.toDiaryUiModel() }
                .launchAsLoadState { loadState ->
                    when (loadState) {
                        is LoadState.Success -> {
                            setPreviewState(loadState.data)
                            saveDiaryToSavedState(loadState.data)
                        }

                        is LoadState.Error -> {
                            when (loadState.exception) {
                                is DiaryNotFoundException -> setNewDiaryState(targetDate)
                                else -> {
                                    loadState.exception?.let { handleDiaryError(it) }
                                    postSideEffect(DiarySideEffect.NavigateToMainScreen)
                                }
                            }
                        }

                        is LoadState.Loading -> reduce { state.copy(diaryLoadState = loadState) }
                        else -> {}
                    }
                }
        }
    }

    private fun setPreviewState(diaryUiModel: DiaryUiModel) = intent {
        reduce {
            state.copy(
                screenMode = DiaryScreenMode.Preview,
                diaryLoadState = LoadState.Success(diaryUiModel)
            )
        }
    }

    private fun setEditState(diaryUiModel: DiaryUiModel) = intent {
        reduce {
            state.copy(
                screenMode = DiaryScreenMode.Edit,
                diaryLoadState = LoadState.Success(diaryUiModel)
            )
        }
    }

    private fun setNewDiaryState(date: LocalDate) = intent {
        reduce {
            state.copy(
                screenMode = DiaryScreenMode.Edit,
                diaryLoadState = LoadState.Success(DiaryUiModel(date = date))
            )
        }
    }

    fun handleIntent(intent: DiaryIntent) {
        when (intent) {
            is DiaryIntent.TitleChanged -> updateTitle(intent.newTitle)
            is DiaryIntent.ContentChanged -> updateContent(intent.newContent)
            is DiaryIntent.DeleteButtonClicked -> deleteDiary()
            is DiaryIntent.EditButtonClicked -> setScreenMode(DiaryScreenMode.Edit)
            is DiaryIntent.DoneButtonClicked -> saveDiary()
        }
    }

    private fun updateTitle(newTitle: String) = intent {
        val diaryUiModel = state.diaryLoadState.data ?: return@intent

        reduce { state.copy(diaryLoadState = LoadState.Success(diaryUiModel.copy(title = newTitle))) }
        savedStateHandle[KEY_TITLE] = newTitle
    }

    private fun updateContent(newContent: String) = intent {
        val diaryUiModel = state.diaryLoadState.data ?: return@intent

        reduce { state.copy(diaryLoadState = LoadState.Success(diaryUiModel.copy(content = newContent))) }
        savedStateHandle[KEY_CONTENT] = newContent
    }

    private fun deleteDiary() = intent {
        val diaryUiModel = state.diaryLoadState.data ?: return@intent

        safeCall { deleteDiaryUseCase(diaryUiModel.toDiary()) }
            .onLoading { reduce { state.copy(isDeleteBtnLoading = it) } }
            .onError { handleDiaryError(it) }
            .launchOnSuccess { postSideEffect(DiarySideEffect.NavigateToMainScreen) }
    }

    private fun setScreenMode(screenMode: DiaryScreenMode) = intent {
        reduce { state.copy(screenMode = screenMode) }
    }

    private fun saveDiary() = intent {
        val diaryUiModel = state.diaryLoadState.data ?: return@intent

        if (diaryUiModel.title.isBlank()) {
            postSideEffect(DiarySideEffect.ShowMsg(UiText.StringResource(R.string.diary_title_is_empty)))
            return@intent
        }

        if (diaryUiModel.content.isBlank()) {
            postSideEffect(DiarySideEffect.ShowMsg(UiText.StringResource(R.string.diary_content_is_empty)))
            return@intent
        }

        safeCall { upsertDiaryUseCase(diaryUiModel.toDiary()) }
            .map { it.toDiaryUiModel() }
            .onLoading { reduce { state.copy(isDoneBtnLoading = it) } }
            .onError { handleDiaryError(it) }
            .launchOnSuccess { diaryUiModel ->
                setPreviewState(diaryUiModel)
                saveDiaryToSavedState(diaryUiModel)
                postSideEffect(DiarySideEffect.ShowMsg(UiText.StringResource(R.string.diary_saved)))
            }
    }

    private fun saveDiaryToSavedState(diaryUiModel: DiaryUiModel) = intent {
        savedStateHandle[KEY_ID] = diaryUiModel.id
        savedStateHandle[KEY_DATE] = diaryUiModel.date.toEpochDay()
        savedStateHandle[KEY_TITLE] = diaryUiModel.title
        savedStateHandle[KEY_CONTENT] = diaryUiModel.content
        savedStateHandle[KEY_EMOTION] = diaryUiModel.emotion
    }

    private fun handleDiaryError(error: Throwable) = intent {
        val message = error.message
            ?.let { UiText.DynamicString(it) }
            ?: UiText.StringResource(R.string.unknown_error)

        postSideEffect(DiarySideEffect.ShowMsg(message))
    }
}
