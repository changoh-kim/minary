package kr.co.presentation.feature.calendar.screen.yearly

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kr.co.core.ui.common.text.getString
import kr.co.core.ui.design.preview.ThemePreviews
import kr.co.core.ui.design.theme.MinaryTheme
import kr.co.presentation.R
import kr.co.presentation.feature.calendar.extension.isCurrentYear
import kr.co.presentation.feature.calendar.model.CalendarGridItem
import kr.co.presentation.feature.calendar.model.CalendarMonthItem
import kr.co.presentation.feature.calendar.model.CalendarYearItem
import kr.co.presentation.feature.calendar.model.year
import kr.co.presentation.feature.calendar.screen.yearly.component.MonthCalendarCanvas
import kr.co.presentation.feature.calendar.screen.yearly.preview.YearlyCalendarScreenPreviewParameterProvider
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.Year
import java.time.YearMonth
import kotlin.math.abs

@Composable
fun YearlyCalendarScreen(
    onMonthClicked: (YearMonth) -> Unit,
    onBack: () -> Unit = {},
    viewModel: YearlyCalendarViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val calendarItems = viewModel.calendarGridItems.collectAsLazyPagingItems()
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val currentVisibleYear by remember {
        derivedStateOf {
            val visibleItems = gridState.layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty() || calendarItems.itemCount == 0) {
                return@derivedStateOf state.visibleYear
            }

            val viewportCenter =
                (gridState.layoutInfo.viewportStartOffset + gridState.layoutInfo.viewportEndOffset) / 2

            val centralItem = visibleItems.minByOrNull {
                val itemCenter = it.offset.y + it.size.height / 2
                abs(itemCenter - viewportCenter)
            }

            centralItem?.index?.let { index ->
                if (index < calendarItems.itemCount) calendarItems.peek(index)?.year else null
            } ?: state.visibleYear
        }
    }

    LaunchedEffect(gridState, calendarItems.itemCount) {
        snapshotFlow { gridState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { isScrolling ->
                // 스크롤이 멈췄을 때만
                if (!isScrolling) {
                    viewModel.handleAction(
                        YearlyCalendarAction.VisibleYearChanged(
                            currentVisibleYear
                        )
                    )
                }
            }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is YearlyCalendarSideEffect.ScrollToInitialPosition -> {
                snapshotFlow { calendarItems.itemCount }
                    .distinctUntilChanged().first { itemCount -> itemCount > 0 }
                    .let { pagingItemCount ->
                        gridState.scrollToItem(pagingItemCount - 1)
                    }
            }
            is YearlyCalendarSideEffect.MonthClicked -> onMonthClicked(sideEffect.targetYearMonth)
            is YearlyCalendarSideEffect.ScrollToToday -> coroutineScope.launch {
                gridState.scrollToItem(calendarItems.itemCount - 1)
            }
            is YearlyCalendarSideEffect.ShowMessage -> coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
            }
        }
    }

    YearlyCalendarContent(
        year = currentVisibleYear,
        calendarItems = calendarItems,
        gridState = gridState,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::handleAction,
        onBack = onBack
    )
}

@Composable
fun YearlyCalendarContent(
    year: Year = Year.now(),
    calendarItems: LazyPagingItems<CalendarGridItem>,
    gridState: LazyGridState = rememberLazyGridState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (YearlyCalendarAction) -> Unit = {},
    onBack: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            YearlyCalendarTopBar(
                modifier = Modifier.fillMaxWidth(),
                year = year,
                onAction = onAction,
                onBack = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        CalendarGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            gridState = gridState,
            calendarItems = calendarItems,
            onAction = onAction,
        )
    }
}

@Composable
private fun YearlyCalendarTopBar(
    modifier: Modifier = Modifier,
    year: Year = Year.now(),
    onAction: (YearlyCalendarAction) -> Unit = {},
    onBack: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .height(64.dp)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color(0xFF64748B))
        }

        Text(
            text = "${year.value}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        OutlinedButton(
            onClick = { onAction(YearlyCalendarAction.TodayClicked) },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = stringResource(R.string.today),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    modifier: Modifier = Modifier,
    calendarItems: LazyPagingItems<CalendarGridItem>,
    gridState: LazyGridState = rememberLazyGridState(),
    onAction: (YearlyCalendarAction) -> Unit = {},
) {
    LazyVerticalGrid(
        modifier = modifier,
        state = gridState,
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        items(
            count = calendarItems.itemCount,
            key = calendarItems.itemKey { item -> item.key },
            contentType = calendarItems.itemContentType { item -> item.contentType },
            span = { index ->
                val item = index
                    .takeIf { it < calendarItems.itemCount }
                    ?.let { calendarItems.peek(it) }

                when (item) {
                    is CalendarYearItem -> GridItemSpan(maxLineSpan)
                    else -> GridItemSpan(1)
                }
            },
        ) { index ->
            if (index < calendarItems.itemCount) {
                val item = calendarItems[index] ?: return@items
                when (item) {
                    is CalendarYearItem -> YearHeader(item)
                    is CalendarMonthItem -> {
                        MonthCalendarCanvas(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(4.dp),
                            monthItem = item,
                            onClick = { onAction(YearlyCalendarAction.MonthClicked(item.yearMonth)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YearHeader(yearItem: CalendarYearItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(
            text = "${yearItem.year.value}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (yearItem.isCurrentYear()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@ThemePreviews
@Composable
private fun YearlyCalendarScreenPreview(
    @PreviewParameter(YearlyCalendarScreenPreviewParameterProvider::class)
    calendarGridItems: Flow<PagingData<CalendarGridItem>>
) {
    val calendarItems = calendarGridItems.collectAsLazyPagingItems()
    MinaryTheme {
        YearlyCalendarContent(
            calendarItems = calendarItems
        )
    }
}