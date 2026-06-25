package kr.co.presentation.feature.dashboard.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import kr.co.core.ui.design.component.skeleton.SkeletonSpacer
import kr.co.core.ui.design.component.skeleton.SkeletonText
import kr.co.core.ui.design.preview.ThemePreviews
import kr.co.core.ui.design.theme.MinaryTheme
import kr.co.presentation.R

@Composable
fun DashboardSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            SkeletonText(
                text = stringResource(R.string.dashboard_insights),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            SkeletonText(
                text = stringResource(R.string.dashboard_deep_analysis),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 히트맵 섹션
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                SkeletonText(
                    text = stringResource(R.string.dashboard_entry_consistency),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                SkeletonEmotionHeatMap()

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SkeletonText(text = "90 DAYS AGO", style = MaterialTheme.typography.labelSmall)
                    SkeletonText(text = "TODAY", style = MaterialTheme.typography.labelSmall)
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
                SkeletonDashboardCard(modifier = Modifier.weight(1f))
                SkeletonDashboardCard(modifier = Modifier.weight(1f))
            }
            SkeletonDashboardCard(modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 감정 기록 섹션
        Column(modifier = Modifier.padding(16.dp)) {
            SkeletonText(
                text = stringResource(R.string.dashboard_emotion_entries),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) {
                    SkeletonSpacer(
                        modifier = Modifier
                            .size(width = 80.dp, height = 32.dp),
                        shape = RoundedCornerShape(9999.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun SkeletonEmotionHeatMap() {
    val columns = 18
    val rows = 5

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(columns) {
                    SkeletonSpacer(
                        modifier = Modifier.size(12.dp),
                        shape = RoundedCornerShape(2.dp)
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
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        SkeletonText(text = "Title Text", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonText(text = "Value Text", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonText(text = "Sub Label", style = MaterialTheme.typography.labelMedium)
    }
}

@ThemePreviews
@Composable
fun DashboardSkeletonPreview() {
    MinaryTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DashboardSkeleton()
        }
    }
}