package kr.co.presentation.feature.dashboard.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kr.co.core.common.model.Emotion
import kr.co.core.ui.common.emotion.resId
import kr.co.core.ui.common.load.LoadStateContentContainer
import kr.co.core.ui.common.text.getString
import kr.co.core.ui.design.preview.ThemePreviews
import kr.co.core.ui.design.theme.BlueGray
import kr.co.core.ui.design.theme.GreenGray
import kr.co.core.ui.design.theme.HeatmapPrimary
import kr.co.core.ui.design.theme.HeatmapQuaternary
import kr.co.core.ui.design.theme.HeatmapSecondary
import kr.co.core.ui.design.theme.HeatmapTertiary
import kr.co.core.ui.design.theme.MinaryTheme
import kr.co.presentation.R
import kr.co.presentation.feature.dashboard.model.DashboardDiaryUiModel
import kr.co.presentation.feature.dashboard.model.DashboardUiModel
import kr.co.presentation.feature.dashboard.screen.dashboard.preview.DashboardScreenPreviewParameterProvider
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is DashboardSideEffect.ShowMessage -> coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
            }
        }
    }

    LoadStateContentContainer(
        loadState = state.dashboardLoadState,
        loading = { DashboardSkeleton() }
    ) { dashboard ->
        DashboardContent(
            snackbarHostState = snackbarHostState,
            dashboard = dashboard,
            onAction = viewModel::handleAction
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardContent(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    dashboard: DashboardUiModel,
    onAction: (DashboardAction) -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_insights),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.dashboard_deep_analysis),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 히트맵 섹션
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_entry_consistency),
                        style = MaterialTheme.typography.labelLarge,
                        color = BlueGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    DiaryHeatMap(recentDiaries = dashboard.recentDiaries)

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_90_days_ago).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = BlueGray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.dashboard_today).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = BlueGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 통계 그리드
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.dashboard_total_entries),
                        value = "${dashboard.totalDiaryCount}",
                        subValue = "+${dashboard.weeklyDiaryCount}",
                        subLabel = stringResource(R.string.dashboard_this_week)
                    )
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.dashboard_longest_streak),
                        value = "${dashboard.longestStreak} ${stringResource(R.string.dashboard_unit_day)}",
                        iconRes = R.drawable.ic_whatshot,
                        subLabel = stringResource(R.string.dashboard_best_ever)
                    )
                }

                DashboardCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.dashboard_total_words),
                    value = "${dashboard.totalWordCount} ${stringResource(R.string.dashboard_unit_word)}",
                    iconRes = R.drawable.ic_insights,
                    subLabel = stringResource(R.string.dashboard_insightful)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 감정 기록 섹션
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.dashboard_emotion_entries),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dashboard.emotionCounts.forEach { (emotion, count) ->
                        EmotionTag(
                            emotion = emotion,
                            count = count,
                            isMostFrequent = emotion == dashboard.emotionCounts.maxByOrNull { it.value }?.key
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun EmotionTag(
    emotion: Emotion,
    count: Int,
    isMostFrequent: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(if (isMostFrequent) MaterialTheme.colorScheme.primary else BlueGray.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "${stringResource(emotion.resId)} ($count)",
            style = MaterialTheme.typography.labelLarge,
            color = if (isMostFrequent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DiaryHeatMap(recentDiaries: List<DashboardDiaryUiModel?>) {
    val columns = 18
    val rows = 5
    
    val heatmapColors = remember {
        listOf(HeatmapPrimary, HeatmapSecondary, HeatmapTertiary, HeatmapQuaternary)
    }

    // 월별 색상 인덱스 계산 로직
    val monthColorIndices = remember(recentDiaries) {
        val indices = IntArray(recentDiaries.size)
        var currentColorIndex = 0
        var lastMonth: Int? = null

        for (i in recentDiaries.indices) {
            val diary = recentDiaries[i]
            val month = diary?.date?.monthValue
            if (month != null && month != lastMonth) {
                if (lastMonth != null) currentColorIndex++
                lastMonth = month
            }
            indices[i] = currentColorIndex
        }
        indices
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(rows) { rowIndex ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(columns) { colIndex ->
                    val index = rowIndex * columns + colIndex
                    val diary = recentDiaries.getOrNull(index)

                    if (diary != null) {
                        val colorIndex = monthColorIndices.getOrElse(index) { 0 }
                        val baseColor = heatmapColors[colorIndex % heatmapColors.size]
                        val alpha = diary.emotions.count().times(0.20f).coerceIn(0.2f, 1f)

                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(baseColor.copy(alpha = alpha))
                        ) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = diary.date.dayOfMonth.toString(),
                                style = TextStyle(
                                    fontSize = 6.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subValue: String? = null,
    subLabel: String? = null,
    iconRes: Int? = null,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = BlueGray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subValue != null || subLabel != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (iconRes != null) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                if (subValue != null) {
                    Text(
                        text = subValue,
                        style = MaterialTheme.typography.labelLarge,
                        color = GreenGray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                if (subLabel != null) {
                    Text(
                        text = subLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = BlueGray
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun DashboardScreenPreview(
    @PreviewParameter(DashboardScreenPreviewParameterProvider::class)
    dashboard: DashboardUiModel,
) {
    MinaryTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DashboardContent(dashboard = dashboard)
        }
    }
}
