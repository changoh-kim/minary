package kr.co.presentation.viewmodel.calendar

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kr.co.domain.model.calendar.CalendarItem
import kr.co.domain.model.calendar.MonthData
import kr.co.domain.model.calendar.isAfterCurrentMonth
import kr.co.domain.usecase.GetCalendarYearUseCase
import kr.co.presentation.R
import kr.co.presentation.ui.model.UiText
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.Year
import javax.inject.Inject


@Immutable
data class YearlyCalendarState(
    val targetYear: Int = Year.now().value,
    val refreshKey: Long = 0L,
)

@Immutable
sealed interface YearlyCalendarSideEffect {
    data class NavigateToMonthlyCalendar(val year: Int, val month: Int) : YearlyCalendarSideEffect
    object ScrollToToday : YearlyCalendarSideEffect
    data class ShowMsg(val uiText: UiText) : YearlyCalendarSideEffect
}

sealed interface YearlyCalendarIntent {
    data class UpdateCalendar(val year: Int) : YearlyCalendarIntent
    object TodayButtonClicked : YearlyCalendarIntent
    data class MonthButtonClicked(val year: Int, val month: Int) : YearlyCalendarIntent
}

@HiltViewModel
class YearlyCalendarViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getCalendarYearUseCase: GetCalendarYearUseCase,
) : ViewModel(), ContainerHost<YearlyCalendarState, YearlyCalendarSideEffect> {

    override val container =
        container<YearlyCalendarState, YearlyCalendarSideEffect>(YearlyCalendarState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val yearPages: Flow<PagingData<CalendarItem>> = container.stateFlow
        .map { state -> (state.targetYear to state.refreshKey) }
        .distinctUntilChanged()
        .flatMapLatest { (targetYear, refreshKey) ->
            getCalendarYearUseCase(targetYear)
        }.cachedIn(viewModelScope)

    fun handleIntent(intent: YearlyCalendarIntent) {
        when (intent) {
            is YearlyCalendarIntent.UpdateCalendar -> updateCalendar(intent.year)
            is YearlyCalendarIntent.TodayButtonClicked -> scrollToToday()
            is YearlyCalendarIntent.MonthButtonClicked -> navigateToMonthlyCalendar(intent.year, intent.month)
        }
    }

    private fun updateCalendar(year: Int) = intent {
        reduce {
            state.copy(
                targetYear = year,
                refreshKey = System.currentTimeMillis()
            )
        }
    }

    private fun scrollToToday() = intent {
        reduce {
            state.copy(
                targetYear = Year.now().value,
                refreshKey = System.currentTimeMillis()
            )
        }
        postSideEffect(YearlyCalendarSideEffect.ScrollToToday)
    }

    private fun navigateToMonthlyCalendar(year: Int, month: Int) = intent {
        val target = MonthData(year, month)
        if (target.isAfterCurrentMonth()) {
            postSideEffect(YearlyCalendarSideEffect.ShowMsg(UiText.StringResource(R.string.you_cannot_select_a_date_after_today)))
        } else {
            postSideEffect(YearlyCalendarSideEffect.NavigateToMonthlyCalendar(year, month))
        }
    }
}