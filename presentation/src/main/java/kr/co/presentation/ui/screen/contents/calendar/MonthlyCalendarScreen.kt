package kr.co.presentation.ui.screen.contents.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kr.co.presentation.R
import kr.co.presentation.ui.component.calendar.CalendarMonth
import kr.co.presentation.ui.component.calendar.day.ActiveDay
import kr.co.presentation.ui.component.calendar.day.InactiveDay
import kr.co.presentation.ui.extension.getString
import kr.co.presentation.ui.extension.stringArrayResource
import kr.co.presentation.ui.model.calendar.day.ActiveDayItem
import kr.co.presentation.ui.model.calendar.day.InactiveDayItem
import kr.co.presentation.ui.model.calendar.yearmonth.MonthItem
import kr.co.presentation.ui.model.common.UiDiary
import kr.co.presentation.ui.preview.CalendarPreviewDataFactory
import kr.co.presentation.ui.theme.MinaryTheme
import kr.co.presentation.viewmodel.calendar.MonthlyCalendarIntent
import kr.co.presentation.viewmodel.calendar.MonthlyCalendarSideEffect
import kr.co.presentation.viewmodel.calendar.MonthlyCalendarViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.LocalDate
import java.time.YearMonth


@Composable
fun MonthlyCalendarScreen(
    viewModel: MonthlyCalendarViewModel = hiltViewModel(),
    onNavigateToDiaryScreen: (LocalDate) -> Unit,
    onNavigateToYearlyCalendar: (Int) -> Unit = {},
) {
    val state by viewModel.collectAsState()
    val pagingItems = viewModel.monthPages.collectAsLazyPagingItems()
    val diariesMap by viewModel.diariesMap.collectAsState(emptyMap())
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pagingItems.itemCount }
    )
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(pagerState, pagingItems.itemCount) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (page < pagingItems.itemCount) {
                    val yearMonth = pagingItems.peek(page)?.yearMonth
                    yearMonth?.let {
                        viewModel.handleIntent(MonthlyCalendarIntent.VisibleMonthChanged(it))
                    }
                }
            }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MonthlyCalendarSideEffect.NavigateToYearlyCalendar -> {
                onNavigateToYearlyCalendar(sideEffect.year)
            }

            is MonthlyCalendarSideEffect.NavigateToDiaryScreen -> {
                onNavigateToDiaryScreen(sideEffect.date)
            }

            is MonthlyCalendarSideEffect.ScrollToToday -> {
                coroutineScope.launch {
                    pagerState.scrollToPage(0)
                }
            }

            is MonthlyCalendarSideEffect.ShowMsg -> coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
            }
        }
    }

    MonthlyCalendarScreen(
        yearMonth = state.visibleYearMonth,
        snackbarHostState = snackbarHostState,
        pagingItems = pagingItems,
        pagerState = pagerState,
        diariesMap = diariesMap,
        intent = viewModel::handleIntent
    )
}

@Composable
private fun MonthlyCalendarScreen(
    yearMonth: YearMonth,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    pagingItems: LazyPagingItems<MonthItem>,
    pagerState: PagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pagingItems.itemCount },
    ),
    diariesMap: Map<LocalDate, UiDiary> = emptyMap(),
    intent: (MonthlyCalendarIntent) -> Unit = {},
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                yearMonth = yearMonth,
                onYearClick = { year ->
                    intent(MonthlyCalendarIntent.YearButtonClicked(year))
                },
                onTodayClick = {
                    intent(MonthlyCalendarIntent.TodayButtonClicked)
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CalendarPager(
                pagerState = pagerState,
                pagingItems = pagingItems,
                diariesMap = diariesMap,
                onDayClick = { dayItem ->
                    intent(MonthlyCalendarIntent.DayClicked(dayItem))
                }
            )
        }
    }
}

@Composable
private fun TopBar(
    yearMonth: YearMonth,
    onYearClick: (Int) -> Unit = {},
    onTodayClick: () -> Unit = {},
) {
    Column {
        Text(
            text = "${yearMonth.year}",
            modifier = Modifier
                .padding(16.dp)
                .clickable { onYearClick(yearMonth.year) }
        )

        val months = stringArrayResource(R.array.months)
        val monthText = months[yearMonth.monthValue - 1]

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
        ) {
            Text(
                text = monthText,
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text = stringResource(R.string.today),
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { onTodayClick() },
            )
        }
    }
}

@Composable
private fun Weekday() {
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
    pagerState: PagerState,
    pagingItems: LazyPagingItems<MonthItem>,
    diariesMap: Map<LocalDate, UiDiary>,
    onDayClick: (ActiveDayItem) -> Unit = {},
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = true,
        verticalAlignment = Alignment.Top,
        beyondViewportPageCount = 1,
        reverseLayout = true,
        key = { index ->
            pagingItems.peek(index)?.key ?: index
        },
    ) { page ->
        val monthItem = pagingItems[page]
        monthItem?.let {
            CalendarMonth(
                monthItem = monthItem,
                headerContent = {
                    Weekday()
                }
            ) { dayItem ->
                when (dayItem) {
                    is ActiveDayItem -> {
                        ActiveDay(
                            modifier = Modifier.weight(1f),
                            dayItem,
                            diary = diariesMap[dayItem.date],
                            isIconVisible = true
                        ) { day -> onDayClick(day) }
                    }

                    is InactiveDayItem -> {
                        InactiveDay(
                            modifier = Modifier.weight(1f),
                            dayItem,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun MonthlyCalendarScreenPreview() {
    val targetYearMonth = YearMonth.now()
    val pagingItems = remember {
        kotlinx.coroutines.flow.flowOf(
            PagingData.from(CalendarPreviewDataFactory.createMonthItems(targetYearMonth))
        )
    }.collectAsLazyPagingItems()

    MinaryTheme {
        MonthlyCalendarScreen(
            yearMonth = targetYearMonth,
            pagingItems = pagingItems,
        )
    }
}