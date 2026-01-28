package kr.co.presentation.ui.screen.contents.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kr.co.presentation.R
import kr.co.presentation.ui.component.calendar.CalendarMonth
import kr.co.presentation.ui.component.calendar.day.ActiveDay
import kr.co.presentation.ui.extension.getString
import kr.co.presentation.ui.extension.isCurrentMonth
import kr.co.presentation.ui.extension.isCurrentYear
import kr.co.presentation.ui.extension.toYearMonth
import kr.co.presentation.ui.model.calendar.day.ActiveDayItem
import kr.co.presentation.ui.model.calendar.day.InactiveDayItem
import kr.co.presentation.ui.model.calendar.yearmonth.MonthItem
import kr.co.presentation.ui.model.calendar.yearmonth.YearItem
import kr.co.presentation.ui.model.calendar.yearmonth.YearMonthItem
import kr.co.presentation.ui.model.calendar.yearmonth.getYear
import kr.co.presentation.ui.preview.CalendarPreviewDataFactory
import kr.co.presentation.ui.theme.MinaryTheme
import kr.co.presentation.viewmodel.calendar.YearlyCalendarIntent
import kr.co.presentation.viewmodel.calendar.YearlyCalendarSideEffect
import kr.co.presentation.viewmodel.calendar.YearlyCalendarViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.Year
import java.time.YearMonth


@Composable
fun YearlyCalendarScreen(
    viewModel: YearlyCalendarViewModel = hiltViewModel(),
    onNavigateToMonthlyCalendar: (YearMonth) -> Unit
) {
    val state by viewModel.collectAsState()
    val pagingItems = viewModel.yearPages.collectAsLazyPagingItems()
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val currentVisibleYear by remember {
        derivedStateOf {
            val visibleItems = gridState.layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty() || pagingItems.itemCount == 0) {
                return@derivedStateOf state.visibleYear
            }

            //val viewportCenter = gridState.layoutInfo.viewportEndOffset / 2
            val viewportCenter = (gridState.layoutInfo.viewportStartOffset + gridState.layoutInfo.viewportEndOffset) / 2

            val centralItem = visibleItems.minByOrNull {
                val itemCenter = it.offset.y + it.size.height / 2
                kotlin.math.abs(itemCenter - viewportCenter)
            }

            centralItem?.index?.let {
                pagingItems.peek(it)?.getYear()
            } ?: state.visibleYear
        }
    }

    LaunchedEffect(gridState, pagingItems.itemCount) {
        snapshotFlow { gridState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { isScrolling ->
                // 스크롤이 멈췄을 때만
                if (!isScrolling) {
                    viewModel.handleIntent(YearlyCalendarIntent.VisibleYearChanged(currentVisibleYear))
                }
            }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is YearlyCalendarSideEffect.NavigateToMonthlyCalendar -> {
                onNavigateToMonthlyCalendar(sideEffect.targetYearMonth)
            }

            is YearlyCalendarSideEffect.ScrollToToday -> coroutineScope.launch {
                gridState.scrollToItem(0)
            }

            is YearlyCalendarSideEffect.ShowMsg -> coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
            }
        }
    }

    YearlyCalendarScreen(
        year = currentVisibleYear,
        pagingItems = pagingItems,
        gridState = gridState,
        snackbarHostState = snackbarHostState,
        intent = viewModel::handleIntent
    )
}

@Composable
private fun YearlyCalendarScreen(
    year: Year = Year.now(),
    pagingItems: LazyPagingItems<YearMonthItem>,
    gridState: LazyGridState = rememberLazyGridState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    intent: (YearlyCalendarIntent) -> Unit = {},
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                modifier = Modifier.fillMaxWidth(),
                year = year,
                onTodayClick = { intent(YearlyCalendarIntent.TodayButtonClicked) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            CalendarGrid(
                modifier = Modifier.fillMaxSize(),
                gridState = gridState,
                pagingItems = pagingItems,
                onMonthClick = { targetYearMonth ->
                    intent(YearlyCalendarIntent.MonthButtonClicked(targetYearMonth))
                }
            )
        }
    }
}

@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    year: Year = Year.now(),
    onTodayClick: () -> Unit = {},
) {
    Box(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterStart),
            text = "${year.value}",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            fontWeight = FontWeight.Bold,
            color = if (year.isCurrentYear()) Color.Red else Color.Black,
        )

        Text(
            text = stringResource(R.string.today),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.Red,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable { onTodayClick() }
        )
    }
}

@Composable
private fun CalendarGrid(
    modifier: Modifier = Modifier,
    pagingItems: LazyPagingItems<YearMonthItem>,
    gridState: LazyGridState = rememberLazyGridState(),
    onMonthClick: (YearMonth) -> Unit = {},
) {
    LazyVerticalGrid(
        modifier = modifier,
        state = gridState,
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        reverseLayout = true,
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { item -> item.key },
            contentType = pagingItems.itemContentType { item -> item.contentType },
            span = { index ->
                val item = pagingItems.peek(index)
                when (item) {
                    is YearItem -> GridItemSpan(maxLineSpan)
                    else -> GridItemSpan(1)
                }
            },
        ) { index ->
            val item = pagingItems[index]
            item?.let { item ->
                when (item) {
                    is YearItem -> YearHeaderItem(item)
                    is MonthItem -> MonthCalendarItem(item, onMonthClick)
                }
            }
        }
    }
}

@Composable
private fun YearHeaderItem(yearItem: YearItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "${yearItem.year.value}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (yearItem.isCurrentYear()) Color.Red else Color.Black
        )
    }
}

@Composable
private fun MonthCalendarItem(
    monthItem: MonthItem,
    onMonthClick: (YearMonth) -> Unit = {},
) {
    CalendarMonth(
        monthItem = monthItem,
        headerContent = {
            val isCurrentMonth = monthItem.isCurrentMonth()
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.month_title, monthItem.yearMonth.monthValue),
                textAlign = TextAlign.Left,
                color = if (isCurrentMonth) Color.Red else Color.Black,
                fontWeight = if (isCurrentMonth) FontWeight.Bold else FontWeight.Normal,
            )
        },
        onClick = onMonthClick,
    ) { dayItem ->
        when (dayItem) {
            is ActiveDayItem -> {
                ActiveDay(
                    modifier = Modifier.weight(1f),
                    dayItem = dayItem,
                    isIconVisible = false
                ) { day ->
                    onMonthClick(day.toYearMonth())
                }
            }

            is InactiveDayItem -> {
                Spacer(
                    Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.small)
                        .background(color = Color.Transparent)
                )
            }
        }
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun YearlyCalendarScreenPreview() {
    val year = Year.now()
    val pagingItems = remember {
        kotlinx.coroutines.flow.flowOf(
            PagingData.from(CalendarPreviewDataFactory.createGridItemsOf(year))
        )
    }.collectAsLazyPagingItems()

    MinaryTheme {
        YearlyCalendarScreen(
            year = year,
            pagingItems = pagingItems
        )
    }
}