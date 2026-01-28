package kr.co.presentation.viewmodel.calendar

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
import kr.co.domain.usecase.calendar.GetCalendarYearUseCase
import kr.co.presentation.R
import kr.co.presentation.mapper.CalendarItemMapper.toYearMonthItem
import kr.co.presentation.ui.extension.isAfterCurrentYearMonth
import kr.co.presentation.ui.model.calendar.yearmonth.YearMonthItem
import kr.co.presentation.ui.model.common.UiText
import kr.co.presentation.ui.navigation.route.YearlyCalender
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.Year
import java.time.YearMonth
import javax.inject.Inject


@Immutable
data class YearlyCalendarState(
    val initYear: Year = Year.now(),
    val refreshKey: Long = 0L,
    val visibleYear: Year = Year.now(),
)

@Immutable
sealed interface YearlyCalendarSideEffect {
    data class NavigateToMonthlyCalendar(val targetYearMonth: YearMonth) : YearlyCalendarSideEffect
    object ScrollToToday : YearlyCalendarSideEffect
    data class ShowMsg(val uiText: UiText) : YearlyCalendarSideEffect
}

sealed interface YearlyCalendarIntent {
    data class VisibleYearChanged(val visibleYear: Year) : YearlyCalendarIntent
    object TodayButtonClicked : YearlyCalendarIntent
    data class MonthButtonClicked(val targetYearMonth: YearMonth) : YearlyCalendarIntent
}

@HiltViewModel
class YearlyCalendarViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getCalendarYearUseCase: GetCalendarYearUseCase,
) : ViewModel(), ContainerHost<YearlyCalendarState, YearlyCalendarSideEffect> {

    companion object {
        private const val KEY_INIT_YEAR = "initYear"
        private const val KEY_REFRESH_KEY = "refreshKey"
        private const val KEY_VISIBLE_YEAR = "visibleYear"
    }

    override val container =
        container<YearlyCalendarState, YearlyCalendarSideEffect>(YearlyCalendarState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val yearPages: Flow<PagingData<YearMonthItem>> = container.stateFlow
        .map { state -> (state.initYear to state.refreshKey) }
        .distinctUntilChanged()
        .flatMapLatest { (targetYear, refreshKey) ->
            getCalendarYearUseCase(targetYear)
        }.map { pagingData ->
            pagingData.map { yearMonthData -> yearMonthData.toYearMonthItem() }
        }.cachedIn(viewModelScope)

    init {
        initializeState()
    }

    private fun initializeState() = intent {
        val yearlyCalender = savedStateHandle.toRoute<YearlyCalender>()

        val initYear = savedStateHandle.get<Int>(KEY_INIT_YEAR) ?: yearlyCalender.year
        val refreshKey = savedStateHandle.get<Long>(KEY_REFRESH_KEY) ?: System.currentTimeMillis()
        val visibleYear = savedStateHandle.get<Int>(KEY_VISIBLE_YEAR) ?: yearlyCalender.year

        reduce {
            state.copy(
                initYear = Year.of(initYear),
                refreshKey = refreshKey,
                visibleYear = Year.of(visibleYear)
            )
        }

        savedStateHandle[KEY_INIT_YEAR] = initYear
        savedStateHandle[KEY_REFRESH_KEY] = refreshKey
        savedStateHandle[KEY_VISIBLE_YEAR] = visibleYear
    }

    fun handleIntent(intent: YearlyCalendarIntent) {
        when (intent) {
            is YearlyCalendarIntent.VisibleYearChanged -> updateVisibleYear(intent.visibleYear)
            is YearlyCalendarIntent.TodayButtonClicked -> scrollToToday()
            is YearlyCalendarIntent.MonthButtonClicked -> navigateToMonthlyCalendar(intent.targetYearMonth)
        }
    }

    private fun updateVisibleYear(visibleYear: Year) = intent {
        reduce { state.copy(visibleYear = visibleYear) }

        savedStateHandle[KEY_VISIBLE_YEAR] = visibleYear.value
    }

    private fun scrollToToday() = intent {
        val currentYear = Year.now()
        val refreshKey = System.currentTimeMillis()

        reduce {
            state.copy(
                initYear = currentYear,
                refreshKey = refreshKey
            )
        }

        savedStateHandle[KEY_INIT_YEAR] = currentYear
        savedStateHandle[KEY_REFRESH_KEY] = refreshKey

        postSideEffect(YearlyCalendarSideEffect.ScrollToToday)
    }

    private fun navigateToMonthlyCalendar(targetYearMonth: YearMonth) = intent {
        if (targetYearMonth.isAfterCurrentYearMonth()) {
            postSideEffect(YearlyCalendarSideEffect.ShowMsg(UiText.StringResource(R.string.you_cannot_select_a_date_after_today)))
        } else {
            postSideEffect(YearlyCalendarSideEffect.NavigateToMonthlyCalendar(targetYearMonth))
        }
    }
}