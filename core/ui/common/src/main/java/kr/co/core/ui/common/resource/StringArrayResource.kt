package kr.co.core.ui.common.resource

import androidx.annotation.ArrayRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

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