package kr.co.presentation.feature.calendar.screen.monthly

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.core.common.model.Emotion
import kr.co.core.common.state.SyncStatus
import kr.co.core.ui.common.emotion.color
import kr.co.core.ui.common.emotion.resId
import kr.co.core.ui.common.resource.stringArrayResource
import kr.co.core.ui.common.text.getString
import kr.co.core.ui.design.preview.ThemePreviews
import kr.co.core.ui.design.theme.MinaryTheme
import kr.co.presentation.R
import kr.co.presentation.feature.calendar.model.CalendarDiaryUiModel
import kr.co.presentation.feature.calendar.model.CalendarMonthItem
import kr.co.presentation.feature.calendar.screen.monthly.component.ActiveDay
import kr.co.presentation.feature.calendar.screen.monthly.component.InactiveDay
import kr.co.presentation.feature.calendar.screen.monthly.component.MonthCalendar
import kr.co.presentation.feature.calendar.screen.monthly.preview.MonthlyCalendarScreenPreviewParameterProvider
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

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
            is MonthlyCalendarSideEffect.YearClicked -> withContext(Dispatchers.Main.immediate) {
                onYearClicked(sideEffect.year)
            }
            is MonthlyCalendarSideEffect.DayClicked -> withContext(Dispatchers.Main.immediate) {
                onDayClicked(sideEffect.date)
            }
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
        onAction = viewModel::handleAction,
        onDayClicked = onDayClicked
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
    onDayClicked: (LocalDate) -> Unit = {},
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MonthlyCalendarTopBar(
                yearMonth = yearMonth,
                onPreviousMonth = {
                    coroutineScope.launch {
                        if (pagerState.currentPage + 1 < monthItems.itemCount) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                onNextMonth = {
                    coroutineScope.launch {
                        if (pagerState.currentPage - 1 >= 0) {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                onAction = onAction
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onDayClicked(LocalDate.now()) },
                modifier = Modifier.testTag("calendar_new_diary_button"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                CalendarPager(
                    modifier = Modifier.fillMaxWidth(),
                    pagerState = pagerState,
                    monthItems = monthItems,
                    onAction = onAction,
                )
            }
            // Today's Diary Section
            val today = LocalDate.now()
            val todayDayItem =
                monthItems.itemSnapshotList.find { it?.yearMonth == YearMonth.from(today) }
                    ?.days?.find { it.date == today }
            val diary = todayDayItem?.diary

            if (diary != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.calendar_todays_diary_label),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 4.dp, bottom = 16.dp)
                    )

                    DiarySummaryCard(diary = diary, onClick = { onDayClicked(today) })
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        SyncProgressOverlay(syncStatus = syncStatus)
    }
}

@Composable
private fun DiarySummaryCard(diary: CalendarDiaryUiModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.3.dp)
    ) {
        Column {
            if (diary.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = diary.imageUrls.first(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = diary.date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (diary.emotions.isNotEmpty()) {
                        EmotionTag(emotion = diary.emotions.first())
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = diary.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = diary.content,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun EmotionTag(emotion: Emotion) {
    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                RoundedCornerShape(9999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(emotion.color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(emotion.resId),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
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
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onAction: (MonthlyCalendarAction) -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
            )
            .height(64.dp)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = Color(0xFF64748B)
                )
            }

            Text(
                text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable { onAction(MonthlyCalendarAction.YearClicked(yearMonth.year)) }
            )

            IconButton(onClick = onNextMonth) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF64748B)
                )
            }
        }

        OutlinedButton(
            onClick = { onAction(MonthlyCalendarAction.TodayClicked) },
            modifier = Modifier.padding(end = 8.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = stringResource(R.string.today),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
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
                text = day.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
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
                    onClick = { onAction(MonthlyCalendarAction.DayClicked(it)) }
                )
            else
                InactiveDay(modifier = Modifier.weight(1f), dayItem = dayItem)
        }
    }
}

@ThemePreviews
@Composable
private fun MonthlyCalendarScreenPreview(
    @PreviewParameter(MonthlyCalendarScreenPreviewParameterProvider::class)
    calendarMonths: Flow<PagingData<CalendarMonthItem>>
) {
    val monthItems = calendarMonths.collectAsLazyPagingItems()
    MinaryTheme {
        MonthlyCalendarContent(
            monthItems = monthItems,
        )
    }
}
