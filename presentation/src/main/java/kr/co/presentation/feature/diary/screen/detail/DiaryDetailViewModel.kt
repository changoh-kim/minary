package kr.co.presentation.feature.diary.screen.detail

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.core.common.error.DomainError
import kr.co.core.common.extension.TAG
import kr.co.core.ui.common.error.handleDomainError
import kr.co.core.ui.common.load.LoadState
import kr.co.core.ui.common.load.data
import kr.co.core.ui.common.load.safeCall
import kr.co.core.ui.common.text.UiText
import kr.co.domain.feature.diary.usecase.DeleteDiaryUseCase
import kr.co.domain.feature.diary.usecase.GetDiaryStreamUseCase
import kr.co.presentation.R
import kr.co.presentation.app.navigation.route.DiaryDetailRoute
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiary
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiaryUiModel
import kr.co.presentation.feature.diary.model.DiaryUiModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import javax.inject.Inject

@Immutable
data class DiaryDetailState(
    val diaryLoadState: LoadState<DiaryUiModel> = LoadState.Uninitialized,
    val isDeleting: Boolean = false,
    val showDeleteDialog: Boolean = false,
)

@Immutable
sealed interface DiaryDetailSideEffect {
    object DiaryDeleted : DiaryDetailSideEffect
    object LoadFailed : DiaryDetailSideEffect
    data class NavigateToEdit(val date: LocalDate, val isNewDiary: Boolean) : DiaryDetailSideEffect
    data class ShowMessage(val uiText: UiText) : DiaryDetailSideEffect
}

sealed interface DiaryDetailAction {
    object DeleteClicked : DiaryDetailAction
    object EditClicked : DiaryDetailAction
    object DeleteConfirmed : DiaryDetailAction
    object DeleteCancelled : DiaryDetailAction
}

@HiltViewModel
class DiaryDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getDiaryStream: GetDiaryStreamUseCase,
    private val deleteDiary: DeleteDiaryUseCase,
) : ViewModel(), ContainerHost<DiaryDetailState, DiaryDetailSideEffect> {

    override val container =
        container<DiaryDetailState, DiaryDetailSideEffect>(DiaryDetailState())

    init {
        loadDiary()
    }

    private fun loadDiary() = intent {
        val route = savedStateHandle.toRoute<DiaryDetailRoute>()
        val targetDate = LocalDate.of(route.year, route.month, route.date)

        getDiaryStream(targetDate).collect { diary ->
            if (diary == null) {
                // 초기 진입 시 데이터가 없는 경우에만 리다이렉트
                if (state.diaryLoadState is LoadState.Uninitialized && !state.isDeleting) {
                    postSideEffect(DiaryDetailSideEffect.NavigateToEdit(targetDate, true))
                }
            } else {
                reduce {
                    state.copy(diaryLoadState = LoadState.Success(diary.toDiaryUiModel()))
                }
            }
        }
    }

    fun handleAction(action: DiaryDetailAction) {
        when (action) {
            is DiaryDetailAction.DeleteClicked -> intent {
                reduce { state.copy(showDeleteDialog = true) }
            }
            is DiaryDetailAction.EditClicked -> intent {
                val diary = state.diaryLoadState.data ?: return@intent
                postSideEffect(DiaryDetailSideEffect.NavigateToEdit(diary.date, false))
            }
            is DiaryDetailAction.DeleteConfirmed -> intent {
                reduce { state.copy(showDeleteDialog = false) }
                requestDeleteDiary()
            }
            is DiaryDetailAction.DeleteCancelled -> intent {
                reduce { state.copy(showDeleteDialog = false) }
            }
        }
    }

    private fun requestDeleteDiary() = intent {
        val diary = state.diaryLoadState.data ?: return@intent

        safeCall<Unit, DomainError> { deleteDiary(diary.toDiary()) }
            .onLoading { reduce { state.copy(isDeleting = it) } }
            .onError { handleDeleteDiaryError(it) }
            .launchOnSuccess { postSideEffect(DiaryDetailSideEffect.DiaryDeleted) }
    }

    private fun handleDeleteDiaryError(error: DomainError) = intent {
        handleDomainError(error) {
            unexpected = { systemError ->
                Log.e(TAG, "Failed to delete diary: An unexpected error has occurred", systemError)
                if (systemError != null) {
                    postSideEffect(DiaryDetailSideEffect.ShowMessage(
                        UiText.StringResource(R.string.unexpected_error)))
                }
            }
        }
    }
}
