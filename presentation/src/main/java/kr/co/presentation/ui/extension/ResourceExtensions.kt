package kr.co.presentation.ui.extension

import android.content.Context
import androidx.annotation.ArrayRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import kr.co.presentation.ui.model.UiText


/**
 * 문자열 배열 리소스를 Composable로 가져오기 위한 확장 함수
 *
 * @param resId 문자열 배열 리소스 ID
 * @return 리소스에 해당하는 [Array<String]
 */
@Composable
fun stringArrayResource(@ArrayRes resId: Int): Array<String> {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(resId, configuration) {
        context.resources.getStringArray(resId)
    }
}

/**
 * [UiText] 객체를 [String] 문자열로 변환하는 [Context] 확장 함수
 *
 * ViewModel에서 전달된 UI 상태([UiText])를 문자열로 변환한다.
 *
 * @param uiText 변환할 [UiText] 객체 (DynamicString 또는 StringResource)
 * @return 로컬라이징이나 인자가 적용된 최종 [String]
 */
fun Context.getString(uiText: UiText): String {
    return when (uiText) {
        is UiText.DynamicString -> uiText.value
        is UiText.StringResource -> getString(uiText.resId, *uiText.args)
    }
}