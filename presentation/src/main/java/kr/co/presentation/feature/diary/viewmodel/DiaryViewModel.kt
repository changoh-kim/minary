package kr.co.presentation.feature.diary.viewmodel

import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import kr.co.domain.error.DomainError
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.diary.usecase.CreateDiaryUseCase
import kr.co.domain.feature.diary.usecase.DeleteDiaryUseCase
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
import kr.co.presentation.navigation.DiaryRoute
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
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isNewDiary: Boolean = false,
)

@Immutable
sealed interface DiarySideEffect {
    object DiaryDeleted : DiarySideEffect
    object LoadFailed : DiarySideEffect
    data class ShowMessage(val uiText: UiText) : DiarySideEffect
}

sealed interface DiaryAction {
    data class TitleChanged(val newTitle: String) : DiaryAction
    data class ContentChanged(val newContent: String) : DiaryAction
    object DeleteClicked : DiaryAction
    object EditClicked : DiaryAction
    object SaveClicked : DiaryAction
}

@OptIn(OrbitExperimental::class)
@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getDiary: GetDiaryUseCase,
    private val deleteDiary: DeleteDiaryUseCase,
    private val createDiary: CreateDiaryUseCase,
    private val updateDiary: UpdateDiaryUseCase,
) : ViewModel(), ContainerHost<DiaryScreenState, DiarySideEffect> {

    private companion object {
        private const val KEY_DIARY = "diary"
        private const val KEY_SCREEN_MODE = "screen_mode"
    }

    override val container =
        container<DiaryScreenState, DiarySideEffect>(DiaryScreenState())

    init {
        loadDiary()
    }

    private fun loadDiary() = intent {
        val route = savedStateHandle.toRoute<DiaryRoute>()
        val targetDate = LocalDate.of(route.year, route.month, route.date)

        val savedDiary: DiaryUiModel? = savedStateHandle[KEY_DIARY]
        val savedScreenMode: DiaryScreenMode? = savedStateHandle[KEY_SCREEN_MODE]

        if (savedDiary != null && (savedDiary.title.isNotBlank() || savedDiary.content.isNotBlank())) {
            updateDiaryMode(savedDiary, savedScreenMode)
        } else {
            safeCall<Diary?, DomainError> { getDiary(targetDate) }
                .map { it?.toDiaryUiModel() }
                .launchAsLoadState { loadState ->
                    when (loadState) {
                        is LoadState.Success -> {
                            if (loadState.data == null) {
                                val newDiary = DiaryUiModel(date = targetDate)
                                reduce { state.copy(isNewDiary = true) }
                                updateDiaryMode(newDiary, DiaryScreenMode.Edit)
                                updateSavedState(newDiary, DiaryScreenMode.Edit)
                            } else {
                                reduce { state.copy(isNewDiary = false) }
                                updateDiaryMode(loadState.data, DiaryScreenMode.Preview)
                                updateSavedState(loadState.data, DiaryScreenMode.Preview)
                            }
                        }
                        is LoadState.Error -> loadState.error?.let { handleGetDiaryError(it) }
                        is LoadState.Loading -> reduce { state.copy(diaryLoadState = loadState) }
                        else -> {}
                    }
                }
        }
    }

    fun handleAction(action: DiaryAction) {
        when (action) {
            is DiaryAction.TitleChanged -> updateTitle(action.newTitle)
            is DiaryAction.ContentChanged -> updateContent(action.newContent)
            is DiaryAction.DeleteClicked -> requestDeleteDiary()
            is DiaryAction.SaveClicked -> requestSaveDiary()
            is DiaryAction.EditClicked -> updateDiaryMode(screenMode = DiaryScreenMode.Edit)
        }
    }

    fun handleGetDiaryError(error: DomainError) = intent {
        handleDomainError(error) {
            unexpected = { systemError ->
                Log.e(TAG, "Failed to get diary: An unexpected error has occurred", systemError)
                if (systemError != null) {
                    postSideEffect(DiarySideEffect.ShowMessage(
                        UiText.StringResource(R.string.unexpected_error)))
                    postSideEffect(DiarySideEffect.LoadFailed)
                }
            }
        }
    }

    private fun handleSaveDiaryError(error: DomainError) = intent {
        handleDomainError(error) {
            unexpected = { systemError ->
                Log.e(TAG, "Failed to save diary: An unexpected error has occurred", systemError)
                if (systemError != null) {
                    postSideEffect(DiarySideEffect.ShowMessage(
                        UiText.StringResource(R.string.unexpected_error)))
                }
            }
        }
    }

    private fun handleDeleteDiaryError(error: DomainError) = intent {
        handleDomainError(error) {
            unexpected = { systemError ->
                Log.e(TAG, "Failed to delete diary: An unexpected error has occurred", systemError)
                if (systemError != null) {
                    postSideEffect(DiarySideEffect.ShowMessage(
                        UiText.StringResource(R.string.unexpected_error)))
                }
            }
        }
    }

    private fun updateTitle(newTitle: String) = blockingIntent {
        val diary = state.diaryLoadState.data ?: return@blockingIntent

        val updateDiary = diary.copy(title = newTitle)
        reduce { state.copy(diaryLoadState = LoadState.Success(updateDiary)) }
        updateSavedState(updateDiary)
    }

    private fun updateContent(newContent: String) = blockingIntent {
        val diary = state.diaryLoadState.data ?: return@blockingIntent

        val updateDiary = diary.copy(content = newContent)
        reduce { state.copy(diaryLoadState = LoadState.Success(updateDiary)) }
        updateSavedState(updateDiary)
    }

    private fun updateDiaryMode(
        diary: DiaryUiModel? = null,
        screenMode: DiaryScreenMode? = null
    ) = intent {
        diary?.let { reduce { state.copy(diaryLoadState = LoadState.Success(it)) } }
        screenMode?.let { reduce { state.copy(screenMode = it) } }
    }

    private fun updateSavedState(
        diary: DiaryUiModel? = null,
        screenMode: DiaryScreenMode? = null
    ) = intent {
        diary?.let { savedStateHandle[KEY_DIARY] = it }
        screenMode?.let { savedStateHandle[KEY_SCREEN_MODE] = it }
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
            .map { it.toDiaryUiModel() }
            .onLoading { reduce { state.copy(isSaving = it) } }
            .onError { handleSaveDiaryError(it) }
            .launchOnSuccess { updatedDiary ->
                reduce { state.copy(isNewDiary = false) }
                updateDiaryMode(updatedDiary, DiaryScreenMode.Preview)
                updateSavedState(updatedDiary, DiaryScreenMode.Preview)
                postSideEffect(DiarySideEffect.ShowMessage(UiText.StringResource(R.string.diary_saved)))
            }
    }

    private fun requestDeleteDiary() = intent {
        val diary = state.diaryLoadState.data ?: return@intent

        safeCall<Unit, DomainError> { deleteDiary(diary.toDiary()) }
            .onLoading { reduce { state.copy(isDeleting = it) } }
            .onError { handleDeleteDiaryError(it) }
            .launchOnSuccess { postSideEffect(DiarySideEffect.DiaryDeleted) }
    }

    private suspend fun SimpleSyntax<DiaryScreenState, DiarySideEffect>.validateInput(diary: DiaryUiModel): Boolean {
        return when {
            diary.title.isBlank() -> {
                postSideEffect(DiarySideEffect.ShowMessage(UiText.StringResource(R.string.diary_title_is_empty)))
                false
            }

            diary.content.isBlank() -> {
                postSideEffect(DiarySideEffect.ShowMessage(UiText.StringResource(R.string.diary_content_is_empty)))
                false
            }

            else -> true
        }
    }
}