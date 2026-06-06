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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kr.co.domain.feature.calendar.usecase.GetCalendarMonthUseCase
import kr.co.domain.feature.diary.model.SyncStatus
import kr.co.domain.feature.diary.usecase.GetDiaryChangeEventUseCase
import kr.co.domain.feature.diary.usecase.sync.GetMonthSyncStatusStreamUseCase
import kr.co.domain.feature.diary.usecase.sync.RequestMonthSyncUseCase
import kr.co.domain.infra.network.usecase.GetNetworkStatusStreamUseCase
import kr.co.presentation.common.model.UiText
import kr.co.presentation.feature.calendar.mapper.CalendarItemMapper.toCalendarMonthItem
import kr.co.presentation.feature.calendar.model.CalendarDayItem
import kr.co.presentation.feature.calendar.model.CalendarMonthItem
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
    val syncStatus: SyncStatus = SyncStatus.IDLE,
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
    object RetrySyncClicked : MonthlyCalendarAction
}

@HiltViewModel
class MonthlyCalendarViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getCalendarMonthUseCase: GetCalendarMonthUseCase,
    private val getMonthSyncStatusStream: GetMonthSyncStatusStreamUseCase,
    private val requestMonthSync: RequestMonthSyncUseCase,
    private val getDiaryChangeEvent: GetDiaryChangeEventUseCase,
    private val getNetworkStatusStream: GetNetworkStatusStreamUseCase,
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val calendarMonthItems: Flow<PagingData<CalendarMonthItem>> =
        container.stateFlow
            .map { state -> (state.initYearMonth to state.refreshKey) }
            .distinctUntilChanged()
            .flatMapLatest { (initYearMonth, refreshKey) ->
                getCalendarMonthUseCase(initYearMonth)
            }.map { pagingData ->
                pagingData.map { calendarMonth ->
                    calendarMonth.toCalendarMonthItem()
                }
            }.cachedIn(viewModelScope)

    init {
        initState()
        collectSyncStatus()
        collectDiaryChangeEvent()
    }

    private fun collectDiaryChangeEvent() = intent {
        getDiaryChangeEvent().collectLatest {
            reduce { state.copy(refreshKey = System.currentTimeMillis()) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun collectSyncStatus() = intent {
        container.stateFlow
            .map { it.visibleYearMonth }
            .distinctUntilChanged()
            .flatMapLatest { visibleYearMonth -> getMonthSyncStatusStream(visibleYearMonth) }
            .collect { status ->
                val prevStatus = state.syncStatus
                reduce { state.copy(syncStatus = status) }

                if (prevStatus == SyncStatus.LOADING && status == SyncStatus.SYNCED) {
                    reduce { state.copy(refreshKey = System.currentTimeMillis()) }
                }

                if (status == SyncStatus.IDLE) {
                    autoRequestSync()
                }
            }
    }

    private fun autoRequestSync() = intent {
        getNetworkStatusStream().collect { isOnline ->
            if (isOnline && state.syncStatus == SyncStatus.IDLE) {
                requestMonthSync(state.visibleYearMonth)
            }
        }
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
            is MonthlyCalendarAction.RetrySyncClicked -> retrySync()
        }
    }

    private fun retrySync() = intent {
        requestMonthSync(state.visibleYearMonth)
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