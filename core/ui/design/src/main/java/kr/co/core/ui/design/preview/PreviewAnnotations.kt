package kr.co.core.ui.design.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

/**
 * 테마(라이트/다크)별 프리뷰
 */
@Preview(
    name = "Light Mode",
    group = "Themes",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Preview(
    name = "Dark Mode",
    group = "Themes",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
annotation class ThemePreviews

/**
 * 주요 기기별 프리뷰 (Phone, Tablet)
 */
@Preview(
    name = "Phone",
    device = Devices.PIXEL_7,
    showSystemUi = true,
    group = "Devices"
)
@Preview(
    name = "Tablet",
    device = Devices.PIXEL_TABLET,
    showSystemUi = true,
    group = "Devices"
)
annotation class DevicePreviews

/**
 * 다국어 지원 프리뷰
 */
@Preview(
    name = "Korean",
    locale = "ko",
    group = "Locales"
)
@Preview(
    name = "English",
    locale = "en",
    group = "Locales"
)
annotation class LocalePreviews

/**
 * 글자 크기(접근성) 테스트 프리뷰
 */
@Preview(
    name = "Small Font",
    fontScale = 0.85f,
    group = "Font Scales"
)
@Preview(
    name = "Large Font",
    fontScale = 1.15f,
    group = "Font Scales"
)
@Preview(
    name = "Extra Large Font",
    fontScale = 1.3f,
    group = "Font Scales"
)
annotation class FontScalePreviews

/**
 * 테마 + 언어를 한 번에 확인하고 싶을 때 사용하는 종합 프리뷰
 */
@ThemePreviews
@LocalePreviews
annotation class MinaryPreviews