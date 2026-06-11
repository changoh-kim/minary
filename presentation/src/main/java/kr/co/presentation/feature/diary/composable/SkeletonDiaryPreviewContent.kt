package kr.co.presentation.feature.diary.composable

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kr.co.presentation.common.skeleton.SkeletonSpacer
import kr.co.presentation.design.ThemePreviews
import kr.co.presentation.theme.MinaryTheme

@Composable
fun SkeletonDiaryPreviewContent() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SkeletonPreviewTopBar()
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Image Placeholder
                SkeletonSpacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Title Placeholder
                SkeletonSpacer(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(32.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Content Placeholders (Multiple lines)
                repeat(5) {
                    SkeletonSpacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                SkeletonSpacer(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(20.dp)
                )
            }

            // Bottom Emotion Section Placeholder
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                SkeletonSpacer(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
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
        }
    }
}

@Composable
private fun SkeletonPreviewTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SkeletonSpacer(modifier = Modifier.size(24.dp), shape = CircleShape)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            SkeletonSpacer(modifier = Modifier.width(120.dp).height(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            SkeletonSpacer(modifier = Modifier.width(60.dp).height(14.dp))
        }
        SkeletonSpacer(modifier = Modifier.size(24.dp), shape = CircleShape)
    }
}

@ThemePreviews
@Composable
private fun SkeletonDiaryPreviewPreview() {
    MinaryTheme {
        SkeletonDiaryPreviewContent()
    }
}