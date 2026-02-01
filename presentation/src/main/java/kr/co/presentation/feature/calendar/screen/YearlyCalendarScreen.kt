package kr.co.presentation.feature.calendar.screen

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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kr.co.presentation.R
import kr.co.presentation.common.extension.getString
import kr.co.presentation.feature.calendar.composable.ActiveDay
import kr.co.presentation.feature.calendar.composable.MonthCalendar
import kr.co.presentation.feature.calendar.composable.MonthCalendarCanvas
import kr.co.presentation.feature.calendar.extension.isCurrentMonth
import kr.co.presentation.feature.calendar.extension.isCurrentYear
import kr.co.presentation.feature.calendar.extension.toYearMonth
import kr.co.presentation.feature.calendar.model.CalendarGridItem
import kr.co.presentation.feature.calendar.model.CalendarMonthItem
import kr.co.presentation.feature.calendar.model.CalendarYearItem
import kr.co.presentation.feature.calendar.model.year
import kr.co.presentation.feature.calendar.preview.provider.YearlyCalendarPreviewDataProvider
import kr.co.presentation.feature.calendar.viewmodel.YearlyCalendarIntent
import kr.co.presentation.feature.calendar.viewmodel.YearlyCalendarSideEffect
import kr.co.presentation.feature.calendar.viewmodel.YearlyCalendarViewModel
import kr.co.presentation.theme.MinaryTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.Year
import java.time.YearMonth
import kotlin.math.abs


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

            val viewportCenter =
                (gridState.layoutInfo.viewportStartOffset + gridState.layoutInfo.viewportEndOffset) / 2

            val centralItem = visibleItems.minByOrNull {
                val itemCenter = it.offset.y + it.size.height / 2
                abs(itemCenter - viewportCenter)
            }

            centralItem?.index?.let { index ->
                if (index < pagingItems.itemCount) {
                    pagingItems.peek(index)?.year
                } else {
                    null
                }
            } ?: state.visibleYear
        }
    }

    LaunchedEffect(gridState, pagingItems.itemCount) {
        snapshotFlow { gridState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { isScrolling ->
                // 스크롤이 멈췄을 때만
                if (!isScrolling) {
                    viewModel.handleIntent(
                        YearlyCalendarIntent.VisibleYearChanged(
                            currentVisibleYear
                        )
                    )
                }
            }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is YearlyCalendarSideEffect.NavigateToMonthlyCalendar -> {
                onNavigateToMonthlyCalendar(sideEffect.targetYearMonth)
            }

            is YearlyCalendarSideEffect.ScrollToToday -> coroutineScope.launch {
                gridState.scrollToItem(pagingItems.itemCount - 1)
            }

            is YearlyCalendarSideEffect.ShowMsg -> coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
            }

            is YearlyCalendarSideEffect.ScrollToInitialPosition -> {
                snapshotFlow { pagingItems.itemCount }
                    .distinctUntilChanged()
                    .filter { itemCount -> itemCount > 0 }
                    .first()
                    .let { pagingItemCount ->
                        gridState.scrollToItem(pagingItemCount - 1)
                    }
            }
        }
    }

    YearlyCalendarContent(
        year = currentVisibleYear,
        pagingItems = pagingItems,
        gridState = gridState,
        snackbarHostState = snackbarHostState,
        intent = viewModel::handleIntent
    )
}

@Composable
fun YearlyCalendarContent(
    year: Year = Year.now(),
    pagingItems: LazyPagingItems<CalendarGridItem>,
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
    pagingItems: LazyPagingItems<CalendarGridItem>,
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
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { item -> item.key },
            contentType = pagingItems.itemContentType { item -> item.contentType },
            span = { index ->
                val item = index
                    .takeIf { it < pagingItems.itemCount }
                    ?.let { pagingItems.peek(it) }

                when (item) {
                    is CalendarYearItem -> GridItemSpan(maxLineSpan)
                    else -> GridItemSpan(1)
                }
            },
        ) { index ->
            if (index < pagingItems.itemCount) {
                val item = pagingItems[index]
                item?.let { item ->
                    when (item) {
                        is CalendarYearItem -> YearHeaderItem(item)
                        is CalendarMonthItem -> MonthCalendarItem(item, onMonthClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun YearHeaderItem(yearItem: CalendarYearItem) {
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
    monthItem: CalendarMonthItem,
    onMonthClick: (YearMonth) -> Unit = {},
) {
    MonthCalendarCanvas(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp),
        monthItem = monthItem,
        onClick = { onMonthClick(monthItem.yearMonth) }
    )
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun YearlyCalendarContentPreview(
    @PreviewParameter(YearlyCalendarPreviewDataProvider::class) previewDataFlow: Flow<PagingData<CalendarGridItem>>
) {
    val pagingItems = previewDataFlow.collectAsLazyPagingItems()
    MinaryTheme {
        YearlyCalendarContent(
            pagingItems = pagingItems
        )
    }
}