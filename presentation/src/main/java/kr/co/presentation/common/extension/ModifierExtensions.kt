package kr.co.presentation.common.extension

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


/**
 * Composable의 배경에 Shimmer 애니메이션을 적용합니다.
 * 스켈레톤 UI(Skeleton UI)의 배경을 표현할 때 유용합니다.
 */
fun Modifier.shimmerBackground(): Modifier = composed {
    // 1. 무한 반복되는 애니메이션 설정
    val transition = rememberInfiniteTransition(label = "shimmer")
    // 2. X축 좌표 이동 애니메이션 (왼쪽 끝 -> 오른쪽 끝)
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    // 3. 그라데이션 브러쉬 생성 (어두운 회색 -> 밝은 회색 -> 어두운 회색)
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.4f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation, y = translateAnimation)
    )

    // 4. 배경으로 적용
    this.background(brush)
}