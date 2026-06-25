package kr.co.core.ui.design.component.skeleton

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import kr.co.core.ui.design.component.skeleton.extension.shimmerBackground

@Composable
fun SkeletonSpacer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp)
) {
    Spacer(
        modifier = modifier
            .clearAndSetSemantics {}
            .clip(shape)
            .shimmerBackground(),
    )
}