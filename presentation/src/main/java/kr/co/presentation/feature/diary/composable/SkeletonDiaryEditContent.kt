package kr.co.presentation.feature.diary.composable

import androidx.compose.foundation.layout.Arrangement
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
fun SkeletonDiaryEditContent() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SkeletonEditTopBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Entry Date Section
            SkeletonSpacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title Input Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonSpacer(modifier = Modifier.width(40.dp).height(16.dp))
                SkeletonSpacer(modifier = Modifier.width(30.dp).height(14.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonSpacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Your Story Input Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonSpacer(modifier = Modifier.width(80.dp).height(16.dp))
                SkeletonSpacer(modifier = Modifier.width(40.dp).height(14.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonSpacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SkeletonEditTopBar() {
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
        SkeletonSpacer(modifier = Modifier.width(100.dp).height(20.dp).weight(1f))
        SkeletonSpacer(modifier = Modifier.size(40.dp), shape = CircleShape)
    }
}

@ThemePreviews
@Composable
private fun SkeletonDiaryEditPreview() {
    MinaryTheme {
        SkeletonDiaryEditContent()
    }
}