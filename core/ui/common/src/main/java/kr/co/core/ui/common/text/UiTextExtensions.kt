package kr.co.core.ui.common.text

import android.content.Context

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