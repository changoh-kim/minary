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
import kr.co.domain.model.calendar.MonthData
import kr.co.domain.usecase.GetCalendarMonthUseCase
import kr.co.presentation.ui.navigation.route.Diary
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.YearMonth
import javax.inject.Inject


@Immutable
data class MonthlyCalendarState(
    val targetYearMonth: YearMonth = YearMonth.now(),
    val refreshKey: Long = 0L,
)

@Immutable
sealed interface MonthlyCalendarSideEffect {
    data class NavigateToYearlyCalendar(val year: Int) : MonthlyCalendarSideEffect
    data class NavigateToDailyCalendar(val diary: Diary) : MonthlyCalendarSideEffect
    object ScrollToToday : MonthlyCalendarSideEffect
    data class ShowMsg(val msg: String) : MonthlyCalendarSideEffect
}

@Immutable
sealed interface MonthlyCalendarIntent {
    // state
    data class UpdateCalendar(val year: Int, val month: Int) : MonthlyCalendarIntent
    // side-effect
    data class NavigateToYearlyCalendar(val year: Int) : MonthlyCalendarIntent
    data class NavigateToDailyCalendar(val diary: Diary) : MonthlyCalendarIntent
    object ScrollToToday : MonthlyCalendarIntent
    data class ShowMsg(val msg: String) : MonthlyCalendarIntent
}

@HiltViewModel
class MonthlyCalendarViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getCalendarMonthUseCase: GetCalendarMonthUseCase,
) : ViewModel(), ContainerHost<MonthlyCalendarState, MonthlyCalendarSideEffect> {

    override val container =
        container<MonthlyCalendarState, MonthlyCalendarSideEffect>(MonthlyCalendarState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthPages: Flow<PagingData<MonthData>> = container.stateFlow
        .map { state -> (state.targetYearMonth to state.refreshKey) }
        .distinctUntilChanged()
        .flatMapLatest { (targetYearMonth, refreshKey) ->
            getCalendarMonthUseCase(targetYearMonth)
        }.cachedIn(viewModelScope)

    fun handleIntent(intent: MonthlyCalendarIntent) = intent {
        when (intent) {
            // state
            is MonthlyCalendarIntent.UpdateCalendar -> updateCalendar(intent.year, intent.month)
            // side-effect
            is MonthlyCalendarIntent.NavigateToYearlyCalendar -> navigateToYearlyCalendar(intent.year)
            is MonthlyCalendarIntent.NavigateToDailyCalendar -> navigateToDailyCalendar(intent.diary)
            is MonthlyCalendarIntent.ScrollToToday -> scrollToToday()
            is MonthlyCalendarIntent.ShowMsg -> showMsg(intent.msg)
        }
    }

    private fun updateCalendar(year: Int, month: Int) = intent {
        reduce {
            state.copy(
                targetYearMonth = YearMonth.of(year, month),
                refreshKey = System.currentTimeMillis()
            )
        }
    }

    private fun navigateToYearlyCalendar(year: Int) = intent {
        postSideEffect(MonthlyCalendarSideEffect.NavigateToYearlyCalendar(year))
    }

    private fun navigateToDailyCalendar(diary: Diary) = intent {
        postSideEffect(MonthlyCalendarSideEffect.NavigateToDailyCalendar(diary))
    }

    private fun scrollToToday() = intent {
        postSideEffect(MonthlyCalendarSideEffect.ScrollToToday)
    }

    private fun showMsg(msg: String) = intent {
        postSideEffect(MonthlyCalendarSideEffect.ShowMsg(msg))
    }
}