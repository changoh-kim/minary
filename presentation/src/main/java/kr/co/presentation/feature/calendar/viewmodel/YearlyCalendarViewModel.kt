package kr.co.presentation.feature.calendar.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kr.co.domain.feature.calendar.usecase.GetCalendarYearUseCase
import kr.co.presentation.R
import kr.co.presentation.common.model.UiText
import kr.co.presentation.feature.calendar.extension.isAfterCurrentYearMonth
import kr.co.presentation.feature.calendar.mapper.CalendarItemMapper.toCalendarMonthItem
import kr.co.presentation.feature.calendar.mapper.insertYearSeparators
import kr.co.presentation.feature.calendar.model.CalendarGridItem
import kr.co.presentation.navigation.YearlyCalendarRoute
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.Year
import java.time.YearMonth
import javax.inject.Inject


@Immutable
data class YearlyCalendarScreenState(
    val initYear: Year = Year.now(),
    val refreshKey: Long = 0L,
    val visibleYear: Year = Year.now(),
)

@Immutable
sealed interface YearlyCalendarSideEffect {
    object ScrollToInitialPosition : YearlyCalendarSideEffect
    data class MonthClicked(val targetYearMonth: YearMonth) : YearlyCalendarSideEffect
    object ScrollToToday : YearlyCalendarSideEffect
    data class ShowMessage(val uiText: UiText) : YearlyCalendarSideEffect
}

sealed interface YearlyCalendarAction {
    data class VisibleYearChanged(val newVisibleYear: Year) : YearlyCalendarAction
    data class MonthClicked(val targetYearMonth: YearMonth) : YearlyCalendarAction
    object TodayClicked : YearlyCalendarAction
}

@HiltViewModel
class YearlyCalendarViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getCalendarYearUseCase: GetCalendarYearUseCase,
) : ViewModel(), ContainerHost<YearlyCalendarScreenState, YearlyCalendarSideEffect> {

    private companion object {
        private const val KEY_INIT_YEAR = "init_year"
        private const val KEY_REFRESH_KEY = "refresh_key"
        private const val KEY_VISIBLE_YEAR = "visible_year"
        private const val KEY_INITIAL_SCROLL_COMPLETED = "initial_scroll_completed"
    }

    override val container =
        container<YearlyCalendarScreenState, YearlyCalendarSideEffect>(YearlyCalendarScreenState())

    /**
     * 연간 달력 Pager에 표시될 페이지 데이터 Flow입니다.
     * `stateFlow`를 기반으로 `initYear` 또는 `refreshKey`가 변경될 때마다 새로운 `PagingData`를 생성합니다.
     *
     * - **`state.initYear`**: Paging의 시작 기준이 되는 연도입니다.
     * - **`state.refreshKey`**: `initYear`가 동일하더라도 Paging을 새로고침하고 싶을 때(예: '오늘' 버튼 클릭)
     *   이 값을 변경하여 `distinctUntilChanged()`를 우회하고 Flow를 강제로 재실행시키는 역할을 합니다.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val calendarGridItems: Flow<PagingData<CalendarGridItem>> = container.stateFlow
        .map { state -> (state.initYear to state.refreshKey) }
        .distinctUntilChanged()
        .flatMapLatest { (initYear, refreshKey) ->
            getCalendarYearUseCase(initYear)
        }.map { pagingData ->
            pagingData.map { calendarMonth ->
                calendarMonth.toCalendarMonthItem()
            }
        }.map { pagingData ->
            pagingData.insertYearSeparators()
        }.cachedIn(viewModelScope)

    init {
        initState()
    }

    private fun initState() = intent {
        val route = savedStateHandle.toRoute<YearlyCalendarRoute>()

        val savedInitYear = savedStateHandle[KEY_INIT_YEAR] ?: route.year
        val savedRefreshKey = savedStateHandle[KEY_REFRESH_KEY] ?: System.currentTimeMillis()
        val savedVisibleYear = savedStateHandle[KEY_VISIBLE_YEAR] ?: route.year
        val savedIsInitialScrollCompleted = savedStateHandle[KEY_INITIAL_SCROLL_COMPLETED] ?: false

        reduce {
            state.copy(
                initYear = Year.of(savedInitYear),
                refreshKey = savedRefreshKey,
                visibleYear = Year.of(savedVisibleYear)
            )
        }

        // 초기 scroll 위치를 한 번만 설정하기 위해 사용
        if (!savedIsInitialScrollCompleted) {
            postSideEffect(YearlyCalendarSideEffect.ScrollToInitialPosition)
            savedStateHandle[KEY_INITIAL_SCROLL_COMPLETED] = true
        }
    }

    fun handleAction(action: YearlyCalendarAction) {
        when (action) {
            is YearlyCalendarAction.VisibleYearChanged -> updateVisibleYear(action.newVisibleYear)
            is YearlyCalendarAction.MonthClicked -> monthClicked(action.targetYearMonth)
            is YearlyCalendarAction.TodayClicked -> todayClicked()
        }
    }

    private fun updateVisibleYear(newVisibleYear: Year) = intent {
        reduce { state.copy(visibleYear = newVisibleYear) }

        savedStateHandle[KEY_VISIBLE_YEAR] = newVisibleYear.value
    }

    private fun monthClicked(targetYearMonth: YearMonth) = intent {
        if (targetYearMonth.isAfterCurrentYearMonth()) {
            postSideEffect(YearlyCalendarSideEffect.ShowMessage(UiText.StringResource(R.string.you_cannot_select_a_date_after_today)))
        } else {
            postSideEffect(YearlyCalendarSideEffect.MonthClicked(targetYearMonth))
        }
    }

    private fun todayClicked() = intent {
        val currentYear = Year.now()
        val refreshKey = System.currentTimeMillis()

        reduce {
            state.copy(
                initYear = currentYear,
                refreshKey = refreshKey
            )
        }

        savedStateHandle[KEY_INIT_YEAR] = currentYear.value
        savedStateHandle[KEY_REFRESH_KEY] = refreshKey

        postSideEffect(YearlyCalendarSideEffect.ScrollToToday)
    }
}