package kr.co.presentation.feature.dashboard.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.presentation.R
import kr.co.presentation.common.skeleton.SkeletonSpacer
import kr.co.presentation.common.skeleton.SkeletonText
import kr.co.presentation.design.ThemePreviews
import kr.co.presentation.theme.MinaryTheme


@Composable
fun SkeletonDashboardContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        SkeletonText(
            text = stringResource(R.string.dashboard_deep_analysis),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        SkeletonText(
            text = stringResource(R.string.dashboard_diary_streak),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 히트맵 스켈레톤
        SkeletonEmotionHeatMap()

        Spacer(modifier = Modifier.height(32.dp))

        SkeletonText(
            text = stringResource(R.string.dashboard_stats),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 통계 카드 스켈레톤
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SkeletonDashboardCard(modifier = Modifier.weight(1f))
            SkeletonDashboardCard(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SkeletonDashboardCard(modifier = Modifier.weight(1f))
            SkeletonDashboardCard(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SkeletonEmotionHeatMap() {
    val columns = 8
    val rows = 5
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(columns) {
                    SkeletonSpacer(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SkeletonDashboardCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SkeletonText(
            text = stringResource(R.string.dashboard_days_written),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        SkeletonText(
            text = "0000",
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonText(
            text = stringResource(R.string.dashboard_unit_day),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@ThemePreviews
@Composable
fun SkeletonDashboardContentPreview() {
    MinaryTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SkeletonDashboardContent()
        }
    }
}