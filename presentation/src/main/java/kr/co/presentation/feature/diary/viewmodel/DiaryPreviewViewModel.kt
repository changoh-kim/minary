package kr.co.presentation.feature.diary.viewmodel

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.domain.error.DomainError
import kr.co.domain.feature.diary.usecase.DeleteDiaryUseCase
import kr.co.domain.feature.diary.usecase.GetDiaryStreamUseCase
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
import kr.co.presentation.navigation.DiaryPreviewRoute
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import javax.inject.Inject

@Immutable
data class DiaryPreviewState(
    val diaryLoadState: LoadState<DiaryUiModel> = LoadState.Uninitialized,
    val isDeleting: Boolean = false,
    val showDeleteDialog: Boolean = false,
)

@Immutable
sealed interface DiaryPreviewSideEffect {
    object DiaryDeleted : DiaryPreviewSideEffect
    object LoadFailed : DiaryPreviewSideEffect
    data class NavigateToEdit(val date: LocalDate, val isNewDiary: Boolean) : DiaryPreviewSideEffect
    data class ShowMessage(val uiText: UiText) : DiaryPreviewSideEffect
}

sealed interface DiaryPreviewAction {
    object DeleteClicked : DiaryPreviewAction
    object EditClicked : DiaryPreviewAction
    object DeleteConfirmed : DiaryPreviewAction
    object DeleteCancelled : DiaryPreviewAction
}

@HiltViewModel
class DiaryPreviewViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getDiaryStream: GetDiaryStreamUseCase,
    private val deleteDiary: DeleteDiaryUseCase,
) : ViewModel(), ContainerHost<DiaryPreviewState, DiaryPreviewSideEffect> {

    override val container =
        container<DiaryPreviewState, DiaryPreviewSideEffect>(DiaryPreviewState())

    init {
        loadDiary()
    }

    private fun loadDiary() = intent {
        val route = savedStateHandle.toRoute<DiaryPreviewRoute>()
        val targetDate = LocalDate.of(route.year, route.month, route.date)

        getDiaryStream(targetDate).collect { diary ->
            if (diary == null) {
                // 초기 진입 시 데이터가 없는 경우에만 리다이렉트
                if (state.diaryLoadState is LoadState.Uninitialized && !state.isDeleting) {
                    postSideEffect(DiaryPreviewSideEffect.NavigateToEdit(targetDate, true))
                }
            } else {
                reduce {
                    state.copy(diaryLoadState = LoadState.Success(diary.toDiaryUiModel()))
                }
            }
        }
    }

    fun handleAction(action: DiaryPreviewAction) {
        when (action) {
            is DiaryPreviewAction.DeleteClicked -> intent {
                reduce { state.copy(showDeleteDialog = true) }
            }
            is DiaryPreviewAction.EditClicked -> intent {
                val diary = state.diaryLoadState.data ?: return@intent
                postSideEffect(DiaryPreviewSideEffect.NavigateToEdit(diary.date, false))
            }
            is DiaryPreviewAction.DeleteConfirmed -> intent {
                reduce { state.copy(showDeleteDialog = false) }
                requestDeleteDiary()
            }
            is DiaryPreviewAction.DeleteCancelled -> intent {
                reduce { state.copy(showDeleteDialog = false) }
            }
        }
    }

    private fun requestDeleteDiary() = intent {
        val diary = state.diaryLoadState.data ?: return@intent

        safeCall<Unit, DomainError> { deleteDiary(diary.toDiary()) }
            .onLoading { reduce { state.copy(isDeleting = it) } }
            .onError { handleDeleteDiaryError(it) }
            .launchOnSuccess { postSideEffect(DiaryPreviewSideEffect.DiaryDeleted) }
    }

    private fun handleDeleteDiaryError(error: DomainError) = intent {
        handleDomainError(error) {
            unexpected = { systemError ->
                Log.e(TAG, "Failed to delete diary: An unexpected error has occurred", systemError)
                if (systemError != null) {
                    postSideEffect(DiaryPreviewSideEffect.ShowMessage(
                        UiText.StringResource(R.string.unexpected_error)))
                }
            }
        }
    }
}
