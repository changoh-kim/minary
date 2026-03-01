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
import kr.co.presentation.R
import kr.co.presentation.common.extension.safeCall
import kr.co.presentation.common.model.UiText
import kr.co.presentation.common.state.LoadState
import kr.co.presentation.common.state.data
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiary
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiaryUiModel
import kr.co.presentation.feature.diary.model.DiaryUiModel
import kr.co.presentation.navigation.DiaryRoute
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.SimpleSyntax
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

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getDiaryUseCase: GetDiaryUseCase,
    private val deleteDiaryUseCase: DeleteDiaryUseCase,
    private val upsertDiaryUseCase: UpsertDiaryUseCase,
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
            // 프로세스 종료 전 작성 중이던 상태가 있다면 바로 복원
            updateDiaryMode(savedDiary, savedScreenMode)
        } else {
            // 작성중인 일기 내용이 없다면, DB에서 가져오기
            safeCall { getDiaryUseCase(targetDate) }
                .map { it.toDiaryUiModel() }
                .launchAsLoadState { loadState ->
                    when (loadState) {
                        is LoadState.Success -> {
                            updateDiaryMode(loadState.data, DiaryScreenMode.Preview)
                            updateSavedState(loadState.data, DiaryScreenMode.Preview)
                        }

                        is LoadState.Error -> {
                            when (loadState.exception) {
                                is DiaryNotFoundException -> {
                                    val newDiary = DiaryUiModel(date = targetDate)
                                    updateDiaryMode(newDiary, DiaryScreenMode.Edit)
                                    updateSavedState(newDiary, DiaryScreenMode.Edit)
                                }

                                else -> {
                                    loadState.exception?.let { handleError(it) }
                                    postSideEffect(DiarySideEffect.LoadFailed)
                                }
                            }
                        }

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

    private fun handleError(error: Throwable) = intent {
        val message = error.message
            ?.let { UiText.DynamicString(it) }
            ?: UiText.StringResource(R.string.unknown_error)

        postSideEffect(DiarySideEffect.ShowMessage(message))
    }

    private fun updateTitle(newTitle: String) = intent {
        val diary = state.diaryLoadState.data ?: return@intent

        val updateDiary = diary.copy(title = newTitle)
        reduce { state.copy(diaryLoadState = LoadState.Success(updateDiary)) }
        updateSavedState(updateDiary)
    }

    private fun updateContent(newContent: String) = intent {
        val diary = state.diaryLoadState.data ?: return@intent

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

        safeCall { upsertDiaryUseCase(diary.toDiary()) }
            .map { it.toDiaryUiModel() }
            .onLoading { reduce { state.copy(isSaving = it) } }
            .onError { handleError(it) }
            .launchOnSuccess { diary ->
                updateDiaryMode(diary, DiaryScreenMode.Preview)
                updateSavedState(diary, DiaryScreenMode.Preview)
                postSideEffect(DiarySideEffect.ShowMessage(UiText.StringResource(R.string.diary_saved)))
            }
    }

    private fun requestDeleteDiary() = intent {
        val diary = state.diaryLoadState.data ?: return@intent

        safeCall { deleteDiaryUseCase(diary.toDiary()) }
            .onLoading { reduce { state.copy(isDeleting = it) } }
            .onError { handleError(it) }
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