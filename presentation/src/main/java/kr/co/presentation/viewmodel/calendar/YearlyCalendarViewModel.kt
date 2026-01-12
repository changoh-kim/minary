package kr.co.presentation.viewmodel.calendar

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kr.co.domain.usecase.GetCalendarYearUseCase
import kr.co.presentation.R
import kr.co.presentation.mapper.CalendarItemMapper.toYearMonthItem
import kr.co.presentation.ui.extension.isAfterCurrentYearMonth
import kr.co.presentation.ui.model.calendar.yearmonth.YearMonthItem
import kr.co.presentation.ui.model.common.UiText
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
    val targetYear: Year = Year.now(),
    val refreshKey: Long = 0L,
)

@Immutable
sealed interface YearlyCalendarSideEffect {
    data class NavigateToMonthlyCalendar(val targetYearMonth: YearMonth) : YearlyCalendarSideEffect
    object ScrollToToday : YearlyCalendarSideEffect
    data class ShowMsg(val uiText: UiText) : YearlyCalendarSideEffect
}

sealed interface YearlyCalendarIntent {
    data class UpdateCalendar(val targetYear: Year) : YearlyCalendarIntent
    object TodayButtonClicked : YearlyCalendarIntent
    data class MonthButtonClicked(val targetYearMonth: YearMonth) : YearlyCalendarIntent
}

@HiltViewModel
class YearlyCalendarViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getCalendarYearUseCase: GetCalendarYearUseCase,
) : ViewModel(), ContainerHost<YearlyCalendarState, YearlyCalendarSideEffect> {

    override val container =
        container<YearlyCalendarState, YearlyCalendarSideEffect>(YearlyCalendarState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val yearPages: Flow<PagingData<YearMonthItem>> = container.stateFlow
        .map { state -> (state.targetYear to state.refreshKey) }
        .distinctUntilChanged()
        .flatMapLatest { (targetYear, refreshKey) ->
            getCalendarYearUseCase(targetYear).map { pagingData ->
                pagingData.map { yearMonthData -> yearMonthData.toYearMonthItem() }
            }
        }.cachedIn(viewModelScope)

    fun handleIntent(intent: YearlyCalendarIntent) {
        when (intent) {
            is YearlyCalendarIntent.UpdateCalendar -> updateCalendar(intent.targetYear)
            is YearlyCalendarIntent.TodayButtonClicked -> scrollToToday()
            is YearlyCalendarIntent.MonthButtonClicked -> navigateToMonthlyCalendar(intent.targetYearMonth)
        }
    }

    private fun updateCalendar(targetYear: Year) = intent {
        reduce {
            state.copy(
                targetYear = targetYear,
                refreshKey = System.currentTimeMillis()
            )
        }
    }

    private fun scrollToToday() = intent {
        reduce {
            state.copy(
                targetYear = Year.now(),
                refreshKey = System.currentTimeMillis()
            )
        }
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