package kr.co.presentation.ui.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable


/**
 * UI에 표시할 텍스트 데이터를 캡슐화하는 Sealed Interface
 *
 * 일반 문자열과 Android 리소스 문자열을 동일한 타입으로 ViewModel에서 UI 계층으로 전달하기 위해 사용
 */
@Immutable
sealed interface UiText {
    /**
     * 일반 [String] 타입의 문자열을 다룰 때 사용
     * @property value 표시할 문자열 값
     */
    data class DynamicString(val value: String) : UiText

    /**
     * 안드로이드 문자열 리소스를 다룰 때 사용
     * @property resId 문자열 리소스 ID
     * @property args 리소스에 전달할 가변 인자
     */
    class StringResource(
        @param:StringRes val resId: Int,
        vararg val args: Any
    ) : UiText {
        // vararg 비교를 위한 equals와 hashCode
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is StringResource) return false
            if (resId != other.resId) return false
            return args.contentEquals(other.args)
        }

        override fun hashCode(): Int {
            var result = resId
            result = 31 * result + args.contentHashCode()
            return result
        }
    }
}