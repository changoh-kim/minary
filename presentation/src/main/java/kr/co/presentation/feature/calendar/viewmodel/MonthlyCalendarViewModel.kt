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
import kr.co.presentation.feature.calendar.navigation.MonthlyCalendarRoute
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiaryUiModel
import kr.co.presentation.feature.diary.model.DiaryUiModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject


@Immutable
data class MonthlyCalendarUiState(
    val initYearMonth: YearMonth = YearMonth.now(),
    val refreshKey: Long = 0L,
    val visibleYearMonth: YearMonth = YearMonth.now(),
)

@Immutable
sealed interface MonthlyCalendarSideEffect {
    data class NavigateToYearlyCalendar(val year: Int) : MonthlyCalendarSideEffect
    data class NavigateToDiaryScreen(val date: LocalDate) : MonthlyCalendarSideEffect
    object ScrollToToday : MonthlyCalendarSideEffect
    data class ShowMsg(val uiText: UiText) : MonthlyCalendarSideEffect
}

sealed interface MonthlyCalendarIntent {
    data class VisibleMonthChanged(val visibleYearMonth: YearMonth) : MonthlyCalendarIntent
    object TodayButtonClicked : MonthlyCalendarIntent
    data class YearButtonClicked(val year: Int) : MonthlyCalendarIntent
    data class DayClicked(val dayItem: CalendarDayItem) : MonthlyCalendarIntent
}

@HiltViewModel
class MonthlyCalendarViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getCalendarMonthUseCase: GetCalendarMonthUseCase,
    private val getDiariesUseCase: GetDiariesUseCase,
) : ViewModel(), ContainerHost<MonthlyCalendarUiState, MonthlyCalendarSideEffect> {

    companion object {
        private const val KEY_INIT_YEAR = "initYear"
        private const val KEY_INIT_MONTH = "initMonth"
        private const val KEY_REFRESH_KEY = "refreshKey"

        private const val KEY_VISIBLE_YEAR = "visibleYear"
        private const val KEY_VISIBLE_MONTH = "visibleMonth"
    }

    override val container =
        container<MonthlyCalendarUiState, MonthlyCalendarSideEffect>(MonthlyCalendarUiState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthPages: Flow<PagingData<CalendarMonthItem>> = container.stateFlow
        .map { state -> (state.initYearMonth to state.refreshKey) }
        .distinctUntilChanged()
        .flatMapLatest { (targetYearMonth, refreshKey) ->
            getCalendarMonthUseCase(targetYearMonth)
        }.map { pagingData ->
            pagingData.map { monthData ->
                monthData.toCalendarMonthItem()
            }
        }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val diariesMap: Flow<Map<LocalDate, DiaryUiModel>> = container.stateFlow
        .map { it.visibleYearMonth }
        .distinctUntilChanged()
        .flatMapLatest { yearMonth ->
            getDiariesUseCase(
                centerMonth = yearMonth,
                monthRange = 1
            )
        }.map { diaryList ->
            diaryList.associate { diaryData ->
                diaryData.date to diaryData.toDiaryUiModel()
            }
        }

    init {
        initializeState()
    }

    private fun initializeState() = intent {
        val route = savedStateHandle.toRoute<MonthlyCalendarRoute>()
        
        val initYear = savedStateHandle.get<Int>(KEY_INIT_YEAR) ?: route.year
        val initMonth = savedStateHandle.get<Int>(KEY_INIT_MONTH) ?: route.month
        val refreshKey = savedStateHandle.get<Long>(KEY_REFRESH_KEY) ?: System.currentTimeMillis()
        val visibleYear = savedStateHandle.get<Int>(KEY_VISIBLE_YEAR) ?: route.year
        val visibleMonth = savedStateHandle.get<Int>(KEY_VISIBLE_MONTH) ?: route.month

        reduce {
            state.copy(
                initYearMonth = YearMonth.of(initYear, initMonth),
                refreshKey = refreshKey,
                visibleYearMonth = YearMonth.of(visibleYear, visibleMonth)
            )
        }

        savedStateHandle[KEY_INIT_YEAR] = initYear
        savedStateHandle[KEY_INIT_MONTH] = initMonth
        savedStateHandle[KEY_REFRESH_KEY] = refreshKey
        savedStateHandle[KEY_VISIBLE_YEAR] = visibleYear
        savedStateHandle[KEY_VISIBLE_MONTH] = visibleMonth
    }

    fun handleIntent(intent: MonthlyCalendarIntent) {
        when (intent) {
            is MonthlyCalendarIntent.VisibleMonthChanged -> updateVisibleMonth(intent.visibleYearMonth)
            is MonthlyCalendarIntent.TodayButtonClicked -> scrollToToday()
            is MonthlyCalendarIntent.YearButtonClicked -> navigateToYearlyCalendar(intent.year)
            is MonthlyCalendarIntent.DayClicked -> navigateToDiaryScreen(intent.dayItem.date)
        }
    }

    private fun updateVisibleMonth(visibleYearMonth: YearMonth) = intent {
        reduce { state.copy(visibleYearMonth = visibleYearMonth) }

        savedStateHandle[KEY_VISIBLE_YEAR] = visibleYearMonth.year
        savedStateHandle[KEY_VISIBLE_MONTH] = visibleYearMonth.monthValue
    }

    private fun scrollToToday() = intent {
        val currentYearMonth = YearMonth.now()
        val refreshKey = System.currentTimeMillis()

        reduce {
            state.copy(
                initYearMonth = currentYearMonth,
                refreshKey = refreshKey
            )
        }

        savedStateHandle[KEY_INIT_YEAR] = currentYearMonth.year
        savedStateHandle[KEY_INIT_MONTH] = currentYearMonth.monthValue
        savedStateHandle[KEY_REFRESH_KEY] = refreshKey

        postSideEffect(MonthlyCalendarSideEffect.ScrollToToday)
    }

    private fun navigateToYearlyCalendar(year: Int) = intent {
        postSideEffect(MonthlyCalendarSideEffect.NavigateToYearlyCalendar(year))
    }

    private fun navigateToDiaryScreen(date: LocalDate) = intent {
        postSideEffect(MonthlyCalendarSideEffect.NavigateToDiaryScreen(date))
    }
}