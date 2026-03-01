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
import kr.co.domain.feature.calendar.usecase.GetCalendarMonthUseCase
import kr.co.domain.feature.diary.usecase.GetDiariesUseCase
import kr.co.presentation.common.model.UiText
import kr.co.presentation.feature.calendar.mapper.CalendarItemMapper.toCalendarMonthItem
import kr.co.presentation.feature.calendar.model.CalendarDayItem
import kr.co.presentation.feature.calendar.model.CalendarMonthItem
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiaryUiModel
import kr.co.presentation.feature.diary.model.DiaryUiModel
import kr.co.presentation.navigation.MonthlyCalendarRoute
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject


@Immutable
data class MonthlyCalendarScreenState(
    val initYearMonth: YearMonth = YearMonth.now(),
    val refreshKey: Long = 0L,
    val visibleYearMonth: YearMonth = YearMonth.now(),
)

@Immutable
sealed interface MonthlyCalendarSideEffect {
    data class YearClicked(val year: Int) : MonthlyCalendarSideEffect
    data class DayClicked(val date: LocalDate) : MonthlyCalendarSideEffect
    object ScrollToToday : MonthlyCalendarSideEffect
    data class ShowMessage(val uiText: UiText) : MonthlyCalendarSideEffect
}

sealed interface MonthlyCalendarAction {
    data class VisibleMonthChanged(val newVisibleYearMonth: YearMonth) : MonthlyCalendarAction
    data class YearClicked(val year: Int) : MonthlyCalendarAction
    data class DayClicked(val dayItem: CalendarDayItem) : MonthlyCalendarAction
    object TodayClicked : MonthlyCalendarAction
}

@HiltViewModel
class MonthlyCalendarViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getCalendarMonthUseCase: GetCalendarMonthUseCase,
    private val getDiariesUseCase: GetDiariesUseCase,
) : ViewModel(), ContainerHost<MonthlyCalendarScreenState, MonthlyCalendarSideEffect> {

    private companion object {
        private const val KEY_INIT_YEAR = "init_year"
        private const val KEY_INIT_MONTH = "init_month"

        private const val KEY_REFRESH_KEY = "refresh_key"

        private const val KEY_VISIBLE_YEAR = "visible_year"
        private const val KEY_VISIBLE_MONTH = "visible_month"
    }

    override val container =
        container<MonthlyCalendarScreenState, MonthlyCalendarSideEffect>(MonthlyCalendarScreenState())

    /**
     * 월간 달력 Pager에 표시될 페이지 데이터 Flow입니다.
     * `stateFlow`를 기반으로 `initYearMonth` 또는 `refreshKey`가 변경될 때마다 새로운 `PagingData`를 생성합니다.
     *
     * - **`state.initYearMonth`**: Paging의 시작 기준이 되는 연월입니다.
     * - **`state.refreshKey`**: `initYearMonth`가 동일하더라도 Paging을 새로고침하고 싶을 때(예: '오늘' 버튼 클릭)
     *   이 값을 변경하여 `distinctUntilChanged()`를 우회하고 Flow를 강제로 재실행시키는 역할을 합니다.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val calendarMonths: Flow<PagingData<CalendarMonthItem>> = container.stateFlow
        .map { state -> (state.initYearMonth to state.refreshKey) }
        .distinctUntilChanged()
        .flatMapLatest { (initYearMonth, refreshKey) ->
            getCalendarMonthUseCase(initYearMonth)
        }.map { pagingData ->
            pagingData.map { calendarMonth ->
                calendarMonth.toCalendarMonthItem()
            }
        }.cachedIn(viewModelScope)

    /**
     * 현재 화면에 보이는 월의 일기 목록을 `Map<LocalDate, DiaryUiModel>` 형태로 제공하는 Flow입니다.
     * `stateFlow`의 `visibleYearMonth`가 변경될 때마다, `getDiariesUseCase`를 호출하여 해당 월의 일기 데이터를 가져옵니다.
     * 데이터를 `Map` 형태로 변환하여, UI(달력)에서 특정 날짜에 일기가 있는지 O(1) 시간 복잡도로 빠르게 조회할 수 있도록 합니다.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val diaries: Flow<Map<LocalDate, DiaryUiModel>> = container.stateFlow
        .map { it.visibleYearMonth }
        .distinctUntilChanged()
        .flatMapLatest { visibleYearMonth ->
            getDiariesUseCase(
                centerMonth = visibleYearMonth,
                monthRange = 1
            )
        }.map { diaryList ->
            diaryList.associate { diary ->
                diary.date to diary.toDiaryUiModel()
            }
        }

    init {
        initState()
    }

    private fun initState() = intent {
        val route = savedStateHandle.toRoute<MonthlyCalendarRoute>()

        val savedInitYear = savedStateHandle[KEY_INIT_YEAR] ?: route.year
        val savedInitMonth = savedStateHandle[KEY_INIT_MONTH] ?: route.month
        val savedRefreshKey = savedStateHandle[KEY_REFRESH_KEY] ?: System.currentTimeMillis()
        val savedVisibleYear = savedStateHandle[KEY_VISIBLE_YEAR] ?: route.year
        val savedVisibleMonth = savedStateHandle[KEY_VISIBLE_MONTH] ?: route.month

        reduce {
            state.copy(
                initYearMonth = YearMonth.of(savedInitYear, savedInitMonth),
                refreshKey = savedRefreshKey,
                visibleYearMonth = YearMonth.of(savedVisibleYear, savedVisibleMonth)
            )
        }
    }

    fun handleAction(action: MonthlyCalendarAction) {
        when (action) {
            is MonthlyCalendarAction.VisibleMonthChanged -> updateVisibleMonth(action.newVisibleYearMonth)
            is MonthlyCalendarAction.YearClicked -> yearClicked(action.year)
            is MonthlyCalendarAction.DayClicked -> dayClicked(action.dayItem.date)
            is MonthlyCalendarAction.TodayClicked -> todayClicked()
        }
    }

    private fun updateVisibleMonth(newVisibleYearMonth: YearMonth) = intent {
        reduce { state.copy(visibleYearMonth = newVisibleYearMonth) }

        savedStateHandle[KEY_VISIBLE_YEAR] = newVisibleYearMonth.year
        savedStateHandle[KEY_VISIBLE_MONTH] = newVisibleYearMonth.monthValue
    }

    private fun yearClicked(year: Int) = intent {
        postSideEffect(MonthlyCalendarSideEffect.YearClicked(year))
    }

    private fun dayClicked(date: LocalDate) = intent {
        postSideEffect(MonthlyCalendarSideEffect.DayClicked(date))
    }

    private fun todayClicked() = intent {
        val currentYearMonth = YearMonth.now()
        val refreshKey = System.currentTimeMillis()

        reduce { state.copy(initYearMonth = currentYearMonth, refreshKey = refreshKey) }

        savedStateHandle[KEY_INIT_YEAR] = currentYearMonth.year
        savedStateHandle[KEY_INIT_MONTH] = currentYearMonth.monthValue
        savedStateHandle[KEY_REFRESH_KEY] = refreshKey

        postSideEffect(MonthlyCalendarSideEffect.ScrollToToday)
    }
}