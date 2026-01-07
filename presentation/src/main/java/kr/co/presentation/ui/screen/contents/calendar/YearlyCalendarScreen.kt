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
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import kotlinx.coroutines.launch
import kr.co.domain.model.calendar.CalendarItem
import kr.co.domain.model.calendar.MonthData
import kr.co.domain.model.calendar.YearData
import kr.co.domain.model.calendar.date.CalendarDateData
import kr.co.domain.model.calendar.date.InactiveDateData
import kr.co.domain.model.calendar.isCurrentMonth
import kr.co.presentation.R
import kr.co.presentation.ui.component.calendar.CalendarMonth
import kr.co.presentation.ui.component.calendar.day.CalendarDay
import kr.co.presentation.ui.extension.getString
import kr.co.presentation.ui.preview.CalendarPreviewData
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
    year: Int = Year.now().value,
    onNavigateToMonthlyCalendar: (Int, Int) -> Unit
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
            if (visibleItems.isEmpty()) return@derivedStateOf year

            val viewportCenter = gridState.layoutInfo.viewportEndOffset / 2

            val centralItem = visibleItems.minByOrNull {
                val itemCenter = (it.offset.y + it.size.height / 2)
                kotlin.math.abs(itemCenter - viewportCenter)
            }

            val dataIndex = centralItem?.index ?: 0
            pagingItems.peek(dataIndex)?.year ?: year
        }
    }

    LaunchedEffect(year) {
        viewModel.handleIntent(YearlyCalendarIntent.UpdateCalendar(year))
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is YearlyCalendarSideEffect.NavigateToMonthlyCalendar -> {
                onNavigateToMonthlyCalendar(sideEffect.year, sideEffect.month)
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
    year: Int,
    pagingItems: LazyPagingItems<CalendarItem>,
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
                onMonthClick = { monthData ->
                    intent(YearlyCalendarIntent.MonthButtonClicked(monthData.year, monthData.month))
                }
            )
        }
    }
}

@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    year: Int = YearMonth.now().year,
    onTodayClick: () -> Unit = {},
) {
    val isCurrentYear = (year == Year.now().value)

    Box(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterStart),
            text = "$year",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            fontWeight = FontWeight.Bold,
            color = if (isCurrentYear) Color.Red else Color.Black,
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
    pagingItems: LazyPagingItems<CalendarItem>,
    gridState: LazyGridState = rememberLazyGridState(),
    onMonthClick: (MonthData) -> Unit = {},
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
                    is YearData -> GridItemSpan(maxLineSpan)
                    else -> GridItemSpan(1)
                }
            },
        ) { index ->
            val item = pagingItems[index]
            item?.let { item ->
                when (item) {
                    is YearData -> YearHeaderItem(year = item.year)
                    is MonthData -> CalendarMonthItem(item, onMonthClick)
                }
            }
        }
    }
}

@Composable
private fun YearHeaderItem(year: Int) {
    val isCurrentYear = Year.now().value == year

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "$year",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isCurrentYear) Color.Red else Color.Black
        )
    }
}

@Composable
private fun CalendarMonthItem(
    monthData: MonthData,
    onMonthClick: (MonthData) -> Unit = {},
) {
    CalendarMonth(
        monthData = monthData,
        headerContent = {
            val isCurrentMonth = monthData.isCurrentMonth()
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.month_title, monthData.month),
                textAlign = TextAlign.Left,
                color = if (isCurrentMonth) Color.Red else Color.Black,
                fontWeight = if (isCurrentMonth) FontWeight.Bold else FontWeight.Normal,
            )
        },
        onClick = onMonthClick,
    ) { dateData ->
        when (dateData) {
            is CalendarDateData -> {
                CalendarDay(
                    modifier = Modifier.weight(1f),
                    dateData = dateData,
                    isIconVisible = false
                ) { diary ->
                    onMonthClick(MonthData(year = diary.year, month = diary.month))
                }
            }

            is InactiveDateData -> Spacer(
                Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.small)
                    .background(color = Color.Transparent)
            )
        }
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun YearlyCalendarScreenPreview() {
    val year = YearMonth.now().year
    val pagingItems = remember {
        kotlinx.coroutines.flow.flowOf(
            PagingData.from(CalendarPreviewData.createYearlyCalendarItems(year))
        )
    }.collectAsLazyPagingItems()

    MinaryTheme {
        YearlyCalendarScreen(
            year = year,
            pagingItems = pagingItems
        )
    }
}