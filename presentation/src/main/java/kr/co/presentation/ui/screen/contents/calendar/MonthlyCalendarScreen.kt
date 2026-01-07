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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import kr.co.domain.model.calendar.MonthData
import kr.co.domain.model.calendar.createYearMonth
import kr.co.domain.model.calendar.date.CalendarDateData
import kr.co.domain.model.calendar.date.InactiveDateData
import kr.co.presentation.R
import kr.co.presentation.ui.component.calendar.CalendarMonth
import kr.co.presentation.ui.component.calendar.day.CalendarDay
import kr.co.presentation.ui.component.calendar.day.OtherMonthDay
import kr.co.presentation.ui.extension.getString
import kr.co.presentation.ui.extension.stringArrayResource
import kr.co.presentation.ui.navigation.route.Diary
import kr.co.presentation.ui.preview.CalendarPreviewData
import kr.co.presentation.ui.theme.MinaryTheme
import kr.co.presentation.viewmodel.calendar.MonthlyCalendarIntent
import kr.co.presentation.viewmodel.calendar.MonthlyCalendarSideEffect
import kr.co.presentation.viewmodel.calendar.MonthlyCalendarViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.YearMonth


@Composable
fun MonthlyCalendarScreen(
    viewModel: MonthlyCalendarViewModel = hiltViewModel(),
    year: Int = YearMonth.now().year,
    month: Int = YearMonth.now().monthValue,
    onNavigateToDiaryScreen: (Diary) -> Unit,
    onNavigateToYearlyCalendar: (Int) -> Unit = {},
) {
    val state by viewModel.collectAsState()
    val pagingItems = viewModel.monthPages.collectAsLazyPagingItems()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pagingItems.itemCount }
    )
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val currentVisibleYearMonth by remember {
        derivedStateOf {
            if (pagingItems.itemCount > 0 && pagerState.currentPage < pagingItems.itemCount) {
                pagingItems.peek(pagerState.currentPage)?.createYearMonth() ?: YearMonth.now()
            } else {
                YearMonth.of(year, month)
            }
        }
    }

    LaunchedEffect(year, month) {
        viewModel.handleIntent(MonthlyCalendarIntent.UpdateCalendar(year, month))
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MonthlyCalendarSideEffect.NavigateToYearlyCalendar -> {
                onNavigateToYearlyCalendar(sideEffect.year)
            }

            is MonthlyCalendarSideEffect.NavigateToDailyCalendar -> {
                onNavigateToDiaryScreen(sideEffect.diary)
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
        yearMonth = currentVisibleYearMonth,
        snackbarHostState = snackbarHostState,
        pagingItems = pagingItems,
        pagerState = pagerState,
        intent = viewModel::handleIntent
    )
}

@Composable
private fun MonthlyCalendarScreen(
    yearMonth: YearMonth,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    pagingItems: LazyPagingItems<MonthData>,
    pagerState: PagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pagingItems.itemCount },
    ),
    intent: (MonthlyCalendarIntent) -> Unit = {},
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                year = yearMonth.year,
                month = yearMonth.monthValue,
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
                onDayClick = { diary ->
                    intent(MonthlyCalendarIntent.DayButtonClicked(diary))
                }
            )
        }
    }
}

@Composable
private fun TopBar(
    year: Int,
    month: Int,
    onYearClick: (Int) -> Unit = {},
    onTodayClick: () -> Unit = {},
) {
    Column {
        Text(
            text = "$year",
            modifier = Modifier
                .padding(16.dp)
                .clickable { onYearClick(year) }
        )

        val months = stringArrayResource(R.array.months)
        val monthText = months[month - 1]

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
    pagingItems: LazyPagingItems<MonthData>,
    onDayClick: (Diary) -> Unit = {},
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
        val monthData = pagingItems[page]
        monthData?.let {
            CalendarMonth(
                monthData = monthData,
                headerContent = {
                    Weekday()
                }
            ) { dayData ->
                when (dayData) {
                    is CalendarDateData -> {
                        CalendarDay(
                            modifier = Modifier.weight(1f),
                            dayData,
                            isIconVisible = true
                        ) { day -> onDayClick(day) }
                    }

                    is InactiveDateData -> OtherMonthDay(
                        modifier = Modifier.weight(1f),
                        dayData.date.toString()
                    )
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
            PagingData.from(CalendarPreviewData.createMonthDataList(targetYearMonth))
        )
    }.collectAsLazyPagingItems()

    MinaryTheme {
        MonthlyCalendarScreen(
            yearMonth = targetYearMonth,
            pagingItems = pagingItems,
        )
    }
}