package kr.co.presentation.testing

import kr.co.core.ui.common.text.UiText
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail

fun assertStringResource(
    expectedResId: Int,
    actual: UiText,
    vararg expectedArgs: Any,
) {
    val stringResource = actual as? UiText.StringResource
        ?: fail("Expected UiText.StringResource, but was ${actual::class.simpleName}.")

    assertEquals(expectedResId, stringResource.resId)
    assertArrayEquals(expectedArgs, stringResource.args)
}
