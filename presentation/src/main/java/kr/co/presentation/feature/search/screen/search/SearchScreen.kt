package kr.co.presentation.feature.search.screen.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.distinctUntilChanged
import kr.co.core.common.model.Emotion
import kr.co.core.ui.common.emotion.color
import kr.co.core.ui.common.emotion.resId
import kr.co.core.ui.design.preview.ThemePreviews
import kr.co.core.ui.design.theme.MinaryTheme
import kr.co.presentation.R
import kr.co.presentation.feature.search.model.SearchDiaryUiModel
import kr.co.presentation.feature.search.screen.search.preview.SearchScreenPreviewParameterProvider
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun SearchScreen(
    onDiaryClicked: (LocalDate) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SearchSideEffect.DiaryClicked -> onDiaryClicked(sideEffect.date)
        }
    }

    SearchContent(
        state = state,
        onAction = viewModel::handleAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    state: SearchScreenState,
    onAction: (SearchAction) -> Unit = {}
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    // 무한 스크롤
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= state.diaries.size - 5) {
                    onAction(SearchAction.LoadNextPage)
                }
            }
    }

    if (showDatePicker) {
        SearchDateRangePicker(
            initialStartDate = state.startDate,
            initialEndDate = state.endDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { start, end ->
                onAction(SearchAction.UpdateDateRange(start, end))
                showDatePicker = false
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
            ) {
                SearchTopBar(title = stringResource(R.string.search_entries_title))
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 일기 목록
            DiaryResultList(
                diaries = state.diaries,
                isLoading = state.isLoading,
                isPaging = state.isPaging,
                listState = listState,
                onDiaryClick = { onAction(SearchAction.DiaryClicked(it)) }
            )
            // 검색창
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                DockedSearchBar(
                    modifier = Modifier.align(Alignment.TopCenter),
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = state.searchQuery,
                            onQueryChange = { onAction(SearchAction.UpdateSearchQuery(it)) },
                            onSearch = {
                                onAction(SearchAction.Search(it))
                                expanded = false
                                keyboardController?.hide()
                            },
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.search_placeholder),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingIcon = {
                                SearchBarLeadingIcon(
                                    expanded = expanded,
                                    onBackClick = { expanded = false }
                                )
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (state.searchQuery.isNotEmpty() || state.startDate != null) {
                                        IconButton(onClick = {
                                            onAction(SearchAction.UpdateSearchQuery(""))
                                            onAction(SearchAction.UpdateDateRange(null, null))
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    IconButton(onClick = { showDatePicker = true }) {
                                        Icon(
                                            imageVector = Icons.Outlined.CalendarMonth,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        )
                    },
                    colors = SearchBarDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shadowElevation = SearchBarDefaults.ShadowElevation,
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    RecentSearchListInBar(
                        recentSearches = state.recentSearches,
                        onSearchClick = {
                            onAction(SearchAction.UpdateSearchQuery(it))
                            onAction(SearchAction.Search(it))
                            expanded = false
                        },
                        onRemoveClick = { onAction(SearchAction.RemoveRecentSearch(it)) },
                        onClearAllClick = { onAction(SearchAction.ClearAllRecentSearches) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBarLeadingIcon(
    expanded: Boolean,
    onBackClick: () -> Unit
) {
    if (expanded) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.profile_common_back_desc),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SearchTopBar(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(56.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun DiaryResultList(
    diaries: List<SearchDiaryUiModel>,
    isLoading: Boolean,
    isPaging: Boolean,
    listState: LazyListState,
    onDiaryClick: (SearchDiaryUiModel) -> Unit
) {
    when {
        isLoading && diaries.isEmpty() -> {
            SearchLoadingState()
        }
        diaries.isEmpty() -> {
            SearchEmptyState()
        }
        else -> {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, top = 72.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(diaries, key = { it.date }) { diary ->
                    DiarySearchCard(
                        diary = diary,
                        onClick = { onDiaryClick(diary) }
                    )
                }
                if (isPaging) {
                    item(key = "paging_indicator") {
                        SearchPagingIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.search_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = stringResource(R.string.no_results_found),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SearchPagingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun RecentSearchListInBar(
    recentSearches: List<String>,
    onSearchClick: (String) -> Unit,
    onRemoveClick: (String) -> Unit,
    onClearAllClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.recent_searches_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            if (recentSearches.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.clear_all),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onClearAllClick() }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (recentSearches.isEmpty()) {
            Text(
                text = stringResource(R.string.no_recent_searches),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            recentSearches.forEach { query ->
                RecentSearchItem(
                    query = query,
                    onClick = { onSearchClick(query) },
                    onRemove = { onRemoveClick(query) }
                )
            }
        }
    }
}

@Composable
fun RecentSearchItem(
    query: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = query,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.dialog_cancel),
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DiarySearchCard(
    diary: SearchDiaryUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            if (diary.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = diary.imageUrls.first(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(21f / 9f),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = diary.date.format(DateTimeFormatter.ofPattern(stringResource(R.string.date_format_full))),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = diary.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                        )
                    }

                    if (diary.emotions.isNotEmpty()) {
                        SearchEmotionTag(emotion = diary.emotions.first())
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = diary.content,
                    style = MaterialTheme.typography.bodyMedium,
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
fun SearchEmotionTag(emotion: Emotion) {
    Surface(
        color = emotion.color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(9999.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(emotion.color, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(emotion.resId),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = emotion.color
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDateRangePicker(
    initialStartDate: LocalDate? = null,
    initialEndDate: LocalDate? = null,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate?, LocalDate?) -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartDate?.atStartOfDay(ZoneOffset.UTC)
            ?.toInstant()?.toEpochMilli(),
        initialSelectedEndDateMillis = initialEndDate?.atStartOfDay(ZoneOffset.UTC)
            ?.toInstant()?.toEpochMilli()
    )

    DatePickerDialog(
        modifier = Modifier.height(470.dp),
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    val end = dateRangePickerState.selectedEndDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    onConfirm(start, end)
                }
            ) {
                Text(stringResource(R.string.dialog_confirm))
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    modifier = Modifier.padding(end = 162.dp),
                    onClick = { onConfirm(null, null) }
                ) {
                    Text(stringResource(R.string.dialog_reset))
                }

                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        }
    ) {
        SearchDateRangePickerContent(state = dateRangePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchDateRangePickerContent(state: DateRangePickerState) {
    val datePattern = stringResource(R.string.date_format_dot)
    val rangeText by remember {
        derivedStateOf {
            val start = state.selectedStartDateMillis?.let {
                Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
            }
            val end = state.selectedEndDateMillis?.let {
                Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
            }

            if (start != null && end != null) {
                "${start.format(DateTimeFormatter.ofPattern(datePattern))} - ${
                    end.format(
                        DateTimeFormatter.ofPattern(datePattern)
                    )
                }"
            } else if (start != null) {
                "${start.format(DateTimeFormatter.ofPattern(datePattern))} - "
            } else {
                null
            }
        }
    }

    DateRangePicker(
        state = state,
        title = {
            Text(
                text = stringResource(R.string.search_select_date_range),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
        },
        headline = {
            Text(
                text = rangeText ?: stringResource(R.string.search_date_range_placeholder),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        },
        showModeToggle = false,
        modifier = Modifier
            .fillMaxWidth()
            .height(462.dp)
    )
}

@ThemePreviews
@Composable
private fun SearchScreenPreview(
    @PreviewParameter(SearchScreenPreviewParameterProvider::class)
    state: SearchScreenState
) {
    MinaryTheme {
        SearchContent(state = state)
    }
}
