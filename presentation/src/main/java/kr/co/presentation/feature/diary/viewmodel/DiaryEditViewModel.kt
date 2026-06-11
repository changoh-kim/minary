package kr.co.presentation.feature.diary.viewmodel

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.domain.error.DomainError
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.diary.usecase.CreateDiaryUseCase
import kr.co.domain.feature.diary.usecase.GetDiaryUseCase
import kr.co.domain.feature.diary.usecase.UpdateDiaryUseCase
import kr.co.presentation.R
import kr.co.presentation.common.extension.TAG
import kr.co.presentation.common.extension.handleDomainError
import kr.co.presentation.common.extension.safeCall
import kr.co.presentation.common.model.UiText
import kr.co.presentation.common.state.LoadState
import kr.co.presentation.common.state.data
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiary
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiaryUiModel
import kr.co.presentation.feature.diary.model.DiaryUiModel
import kr.co.presentation.navigation.DiaryEditRoute
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.syntax.simple.SimpleSyntax
import org.orbitmvi.orbit.syntax.simple.blockingIntent
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import javax.inject.Inject

@Immutable
data class DiaryEditState(
    val diaryLoadState: LoadState<DiaryUiModel> = LoadState.Uninitialized,
    val isSaving: Boolean = false,
    val isNewDiary: Boolean = false,
)

@Immutable
sealed interface DiaryEditSideEffect {
    object DiarySaved : DiaryEditSideEffect
    object LoadFailed : DiaryEditSideEffect
    data class ShowMessage(val uiText: UiText) : DiaryEditSideEffect
    object MaxCharLimitReached : DiaryEditSideEffect
}

sealed interface DiaryEditAction {
    data class TitleChanged(val newTitle: String) : DiaryEditAction
    data class ContentChanged(val newContent: String) : DiaryEditAction
    object SaveClicked : DiaryEditAction
}

@OptIn(OrbitExperimental::class)
@HiltViewModel
class DiaryEditViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getDiary: GetDiaryUseCase,
    private val createDiary: CreateDiaryUseCase,
    private val updateDiary: UpdateDiaryUseCase,
) : ViewModel(), ContainerHost<DiaryEditState, DiaryEditSideEffect> {

    override val container =
        container<DiaryEditState, DiaryEditSideEffect>(DiaryEditState())

    init {
        loadDiary()
    }

    private fun loadDiary() = intent {
        val route = savedStateHandle.toRoute<DiaryEditRoute>()
        val targetDate = LocalDate.of(route.year, route.month, route.date)
        val isNewDiary = route.isNewDiary

        reduce { state.copy(isNewDiary = isNewDiary) }

        if (isNewDiary) {
            reduce {
                state.copy(
                    diaryLoadState = LoadState.Success(DiaryUiModel(date = targetDate))
                )
            }
        } else {
            safeCall<Diary?, DomainError> { getDiary(targetDate) }
                .map { it?.toDiaryUiModel() }
                .launchAsLoadState { loadState ->
                    when (loadState) {
                        is LoadState.Success -> {
                            if (loadState.data == null) {
                                postSideEffect(DiaryEditSideEffect.LoadFailed)
                            } else {
                                reduce { state.copy(diaryLoadState = LoadState.Success(loadState.data!!)) }
                            }
                        }
                        is LoadState.Error -> loadState.error?.let { handleGetDiaryError(it) }
                        is LoadState.Loading -> reduce { state.copy(diaryLoadState = loadState) }
                        else -> {}
                    }
                }
        }
    }

    fun handleAction(action: DiaryEditAction) {
        when (action) {
            is DiaryEditAction.TitleChanged -> updateTitle(action.newTitle)
            is DiaryEditAction.ContentChanged -> updateContent(action.newContent)
            is DiaryEditAction.SaveClicked -> requestSaveDiary()
        }
    }

    private fun updateTitle(newTitle: String) = blockingIntent {
        val diary = state.diaryLoadState.data ?: return@blockingIntent
        if (newTitle.length > MAX_TITLE_LENGTH) {
            postSideEffect(DiaryEditSideEffect.MaxCharLimitReached)
            return@blockingIntent
        }
        reduce {
            state.copy(
                diaryLoadState = LoadState.Success(diary.copy(title = newTitle))
            )
        }
    }

    private fun updateContent(newContent: String) = blockingIntent {
        val diary = state.diaryLoadState.data ?: return@blockingIntent
        if (newContent.length > MAX_CONTENT_LENGTH) {
            postSideEffect(DiaryEditSideEffect.MaxCharLimitReached)
            return@blockingIntent
        }
        reduce {
            state.copy(
                diaryLoadState = LoadState.Success(diary.copy(content = newContent))
            )
        }
    }

    private fun requestSaveDiary() = intent {
        val diary = state.diaryLoadState.data ?: return@intent

        if (!validateInput(diary)) return@intent

        val resultFlow = if (state.isNewDiary) {
            safeCall<Unit, DomainError> { createDiary(diary.toDiary()) }.map { diary.toDiary() }
        } else {
            safeCall<Diary, DomainError> { updateDiary(diary.toDiary()) }
        }

        resultFlow
            .onLoading { reduce { state.copy(isSaving = it) } }
            .onError { handleSaveDiaryError(it) }
            .launchOnSuccess {
                postSideEffect(DiaryEditSideEffect.ShowMessage(UiText.StringResource(R.string.diary_saved)))
                postSideEffect(DiaryEditSideEffect.DiarySaved)
            }
    }

    private fun handleGetDiaryError(error: DomainError) = intent {
        handleDomainError(error) {
            unexpected = { systemError ->
                Log.e(TAG, "Failed to get diary: An unexpected error has occurred", systemError)
                if (systemError != null) {
                    postSideEffect(DiaryEditSideEffect.ShowMessage(
                        UiText.StringResource(R.string.unexpected_error)))
                    postSideEffect(DiaryEditSideEffect.LoadFailed)
                }
            }
        }
    }

    private fun handleSaveDiaryError(error: DomainError) = intent {
        handleDomainError(error) {
            unexpected = { systemError ->
                Log.e(TAG, "Failed to save diary: An unexpected error has occurred", systemError)
                if (systemError != null) {
                    postSideEffect(DiaryEditSideEffect.ShowMessage(
                        UiText.StringResource(R.string.unexpected_error)))
                }
            }
        }
    }

    private suspend fun SimpleSyntax<DiaryEditState, DiaryEditSideEffect>.validateInput(diary: DiaryUiModel): Boolean {
        return when {
            diary.title.isBlank() -> {
                postSideEffect(DiaryEditSideEffect.ShowMessage(UiText.StringResource(R.string.diary_title_is_empty)))
                false
            }
            diary.content.isBlank() -> {
                postSideEffect(DiaryEditSideEffect.ShowMessage(UiText.StringResource(R.string.diary_content_is_empty)))
                false
            }
            else -> true
        }
    }

    companion object {
        const val MAX_TITLE_LENGTH = 50
        const val MAX_CONTENT_LENGTH = 500
    }
}
