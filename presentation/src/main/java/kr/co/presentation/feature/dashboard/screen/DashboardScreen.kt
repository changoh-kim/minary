package kr.co.presentation.feature.dashboard.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kr.co.domain.feature.emotion.Emotion
import kr.co.presentation.R
import kr.co.presentation.common.composable.LoadStateContent
import kr.co.presentation.common.extension.color
import kr.co.presentation.common.extension.getString
import kr.co.presentation.common.extension.resId
import kr.co.presentation.feature.dashboard.composable.SkeletonDashboardContent
import kr.co.presentation.feature.dashboard.model.DashboardUiModel
import kr.co.presentation.feature.dashboard.preview.DashboardPreviewDataProvider
import kr.co.presentation.feature.dashboard.viewmodel.DashboardAction
import kr.co.presentation.feature.dashboard.viewmodel.DashboardSideEffect
import kr.co.presentation.feature.dashboard.viewmodel.DashboardViewModel
import kr.co.presentation.theme.MinaryTheme
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LoadStateContent(
            loadState = state.dashboardLoadState,
            loading = { SkeletonDashboardContent(modifier = Modifier.padding(paddingValues)) }
        ) { dashboard ->
            DashboardContent(
                modifier = Modifier.padding(paddingValues),
                dashboard = dashboard,
                onAction = viewModel::handleAction
            )
        }
    }
}

@Composable
fun DashboardContent(
    modifier: Modifier = Modifier,
    dashboard: DashboardUiModel,
    onAction: (DashboardAction) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.dashboard_deep_analysis),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.dashboard_diary_streak),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 히트맵 그리드 (8 x 5 예시)
        EmotionHeatMap(emotions = dashboard.recentEmotions)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.dashboard_stats),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 통계 카드 그리드
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DashboardCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_days_written),
                value = "${dashboard.totalDiaries}",
                unit = stringResource(R.string.dashboard_unit_day)
            )
            DashboardCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_total_words),
                value = "${dashboard.totalWords}",
                unit = stringResource(R.string.dashboard_unit_word)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DashboardCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_dominant_emotion),
                value = stringResource(dashboard.dominantEmotion.resId),
                valueColor = dashboard.dominantEmotion.color
            )
            DashboardCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_rarest_emotion),
                value = stringResource(dashboard.rarestEmotion.resId),
                valueColor = dashboard.rarestEmotion.color
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun EmotionHeatMap(emotions: List<Emotion>) {
    val columns = 8
    val rows = 5

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(rows) { rowIndex ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(columns) { colIndex ->
                    val index = rowIndex * columns + colIndex
                    val emotion = emotions.getOrNull(index) ?: Emotion.UNKNOWN
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (emotion == Emotion.UNKNOWN) Color(0xFFE9F0F8) else emotion.color)
                    )
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
    unit: String? = null,
    valueColor: Color = Color.Black
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE9F0F8))
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
            fontWeight = FontWeight.Bold,
            color = if (valueColor == Color.Black) Color.White else valueColor // 이미지처럼 흰색 배경에 대비되게 하려면 조정 필요
        )
        unit?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray.copy(alpha = 0.6f)
            )
        }
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
fun DashboardContentPreview(
    @PreviewParameter(DashboardPreviewDataProvider::class) dashboard: DashboardUiModel,
) {
    MinaryTheme {
        DashboardContent(dashboard = dashboard)
    }
}