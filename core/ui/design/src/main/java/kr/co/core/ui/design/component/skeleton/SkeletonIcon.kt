package kr.co.core.ui.design.component.skeleton

import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.clearAndSetSemantics
import kr.co.core.ui.design.component.skeleton.extension.shimmerBackground

@Composable
fun SkeletonIconButton(
    modifier: Modifier = Modifier,
    shape: Shape = IconButtonDefaults.filledShape,
    content: @Composable () -> Unit,
) {
    IconButton(
        modifier = modifier
            .clearAndSetSemantics {}
            .clip(shape)
            .shimmerBackground(),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Transparent
        ),
        onClick = { },
        content = content,
    )
}