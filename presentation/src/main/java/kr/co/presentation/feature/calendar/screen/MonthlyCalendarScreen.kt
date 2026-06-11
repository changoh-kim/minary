package kr.co.presentation.feature.calendar.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kr.co.domain.feature.diary.model.SyncStatus
import kr.co.presentation.R
import kr.co.presentation.common.composable.stringArrayResource
import kr.co.presentation.common.extension.getString
import kr.co.presentation.design.ThemePreviews
import kr.co.presentation.feature.calendar.composable.ActiveDay
import kr.co.presentation.feature.calendar.composable.InactiveDay
import kr.co.presentation.feature.calendar.composable.MonthCalendar
import kr.co.presentation.feature.calendar.model.CalendarMonthItem
import kr.co.presentation.feature.calendar.preview.provider.CalendarMonthItemPreviewDataProvider
import kr.co.presentation.feature.calendar.viewmodel.MonthlyCalendarAction
import kr.co.presentation.feature.calendar.viewmodel.MonthlyCalendarSideEffect
import kr.co.presentation.feature.calendar.viewmodel.MonthlyCalendarViewModel
import kr.co.presentation.theme.MinaryTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthlyCalendarScreen(
    onYearClicked: (Int) -> Unit = {},
    onDayClicked: (LocalDate) -> Unit,
    viewModel: MonthlyCalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val monthItems = viewModel.calendarMonthItems.collectAsLazyPagingItems()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { monthItems.itemCount }
    )
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(pagerState, monthItems.itemCount) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (page < monthItems.itemCount) {
                    val yearMonth = monthItems.peek(page)?.yearMonth
                    yearMonth?.let {
                        viewModel.handleAction(MonthlyCalendarAction.VisibleMonthChanged(it))
                    }
                }
            }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MonthlyCalendarSideEffect.YearClicked -> onYearClicked(sideEffect.year)
            is MonthlyCalendarSideEffect.DayClicked -> onDayClicked(sideEffect.date)
            is MonthlyCalendarSideEffect.ScrollToToday -> coroutineScope.launch {
                pagerState.scrollToPage(0)
            }
            is MonthlyCalendarSideEffect.ShowMessage -> coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
            }
        }
    }

    MonthlyCalendarContent(
        yearMonth = state.visibleYearMonth,
        syncStatus = state.syncStatus,
        snackbarHostState = snackbarHostState,
        monthItems = monthItems,
        pagerState = pagerState,
        onAction = { action -> viewModel.handleAction(action) }
    )
}

@Composable
fun MonthlyCalendarContent(
    yearMonth: YearMonth = YearMonth.now(),
    syncStatus: SyncStatus = SyncStatus.IDLE,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    monthItems: LazyPagingItems<CalendarMonthItem>,
    pagerState: PagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { monthItems.itemCount },
    ),
    onAction: (MonthlyCalendarAction) -> Unit = {},
) {
    val context = LocalContext.current

    LaunchedEffect(syncStatus) {
        if (syncStatus == SyncStatus.FAILED) {
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.sync_failed),
                actionLabel = context.getString(R.string.retry),
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                onAction(MonthlyCalendarAction.RetrySyncClicked)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { MonthlyCalendarTopBar(yearMonth = yearMonth, onAction = onAction) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            CalendarPager(
                modifier = Modifier.fillMaxSize(),
                pagerState = pagerState,
                monthItems = monthItems,
                onAction = onAction,
            )

            SyncProgressOverlay(syncStatus = syncStatus)
        }
    }
}

@Composable
private fun SyncProgressOverlay(syncStatus: SyncStatus) {
    AnimatedVisibility(
        visible = syncStatus == SyncStatus.LOADING,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun MonthlyCalendarTopBar(
    yearMonth: YearMonth,
    onAction: (MonthlyCalendarAction) -> Unit = {},
) {
    Column {
        Text(
            text = "${yearMonth.year}",
            modifier = Modifier
                .padding(16.dp)
                .clickable { onAction(MonthlyCalendarAction.YearClicked(yearMonth.year)) }
        )

        val months = stringArrayResource(R.array.months)
        val monthText = months[yearMonth.monthValue - 1]

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
        ) {
            Text(text = monthText, modifier = Modifier.padding(16.dp))

            Text(
                text = stringResource(R.string.today),
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { onAction(MonthlyCalendarAction.TodayClicked) },
            )
        }
    }
}

@Composable
private fun WeekdayHeader() {
    val weekday = stringArrayResource(R.array.weekdays)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekday.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CalendarPager(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    monthItems: LazyPagingItems<CalendarMonthItem>,
    onAction: (MonthlyCalendarAction) -> Unit = {},
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        userScrollEnabled = true,
        verticalAlignment = Alignment.Top,
        beyondViewportPageCount = 1,
        reverseLayout = true,
        key = { index -> monthItems.peek(index)?.key ?: index },
    ) { page ->
        val monthItem = monthItems[page] ?: return@HorizontalPager

        MonthCalendar(
            monthItem = monthItem,
            header = { WeekdayHeader() }
        ) { dayItem ->
            if (dayItem.isCurrentMonth)
                ActiveDay(
                    modifier = Modifier.weight(1f),
                    dayItem = dayItem,
                    diary = dayItem.diary,
                ) { day -> onAction(MonthlyCalendarAction.DayClicked(day)) }
            else
                InactiveDay(modifier = Modifier.weight(1f), dayItem)
        }
    }
}

@ThemePreviews
@Composable
private fun MonthlyCalendarScreenPreview(
    @PreviewParameter(CalendarMonthItemPreviewDataProvider::class)
    calendarMonths: Flow<PagingData<CalendarMonthItem>>
) {
    MonthlyCalendarPreviewContent(calendarMonths)
}

@Composable
fun MonthlyCalendarPreviewContent(
    calendarMonths: Flow<PagingData<CalendarMonthItem>>
) {
    val monthItems = calendarMonths.collectAsLazyPagingItems()
    MinaryTheme {
        MonthlyCalendarContent(
            monthItems = monthItems,
        )
    }
}