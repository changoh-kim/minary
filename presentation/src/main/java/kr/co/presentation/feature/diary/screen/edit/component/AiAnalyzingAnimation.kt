package kr.co.presentation.feature.diary.screen.edit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kr.co.core.ui.design.theme.MinaryTheme
import kr.co.core.ui.design.theme.NightSky
import kr.co.presentation.R

@Composable
fun AiAnalyzingAnimation(
    modifier: Modifier = Modifier,
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.anim_ai_analyzing)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(250.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun AiAnalyzingAnimationPreview() {
    MinaryTheme {
        Box(
            modifier = Modifier.background(NightSky.copy(alpha = 0.9f))
        ) {
            AiAnalyzingAnimation()
        }
    }
}
