package kr.co.presentation.common.skeleton

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.clearAndSetSemantics
import kr.co.presentation.common.extension.shimmerBackground


@Composable
fun SkeletonButton(
    modifier: Modifier = Modifier,
    shape: Shape = ButtonDefaults.shape,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        modifier = modifier
            .clearAndSetSemantics{}
            .clip(shape) // 버튼 쉐이프 (RoundedCornerShape 등)
            .shimmerBackground(), // ✨ 버튼 전체 배경이 반짝이게 하거나,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Transparent
        ),
        onClick = {},
        content = content
    )
}