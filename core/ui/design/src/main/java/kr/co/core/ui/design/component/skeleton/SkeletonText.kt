package kr.co.core.ui.design.component.skeleton

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kr.co.core.ui.design.component.skeleton.extension.shimmerBackground

@Composable
fun SkeletonText(
    modifier: Modifier = Modifier,
    text: String,
    shape: Shape = RoundedCornerShape(4.dp),
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    Text(
        text = text,
        modifier = modifier
            .clearAndSetSemantics {}
            .clip(shape)
            .shimmerBackground(),
        color = Color.Transparent,
        style = style,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        textAlign = textAlign,
    )
}