package kr.co.presentation.feature.diary.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kr.co.presentation.R
import kr.co.presentation.common.skeleton.SkeletonButton
import kr.co.presentation.common.skeleton.SkeletonIconButton
import kr.co.presentation.common.skeleton.SkeletonSpacer
import kr.co.presentation.design.ThemePreviews
import kr.co.presentation.theme.MinaryTheme


@Composable
fun SkeletonDiaryContent() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SkeletonDiaryTopBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SkeletonSpacer(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(0.5f)
                    .height(20.dp)
            )
            SkeletonSpacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(start = 16.dp, end = 16.dp)

            )
            SkeletonSpacer(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            )
        }
    }
}

@Composable
fun SkeletonDiaryTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        SkeletonSpacer(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(24.dp),
            shape = CircleShape
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonIconButton {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_delete),
                    contentDescription = ""
                )
            }

            SkeletonButton {
                Text(stringResource(R.string.diary_edit_button))
            }
        }
    }
}

@ThemePreviews
@Composable
private fun SkeletonDiaryContentPreview() {
    MinaryTheme {
        SkeletonDiaryContent()
    }
}