package kr.co.core.ui.common.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class UiTextTest {

    @Test
    fun `dynamic string equality uses value`() {
        assertEquals(UiText.DynamicString("value-test"), UiText.DynamicString("value-test"))
        assertNotEquals(UiText.DynamicString("value-test"), UiText.DynamicString("other-value-test"))
    }

    @Test
    fun `string resource equality uses resource id and args`() {
        val first = UiText.StringResource(1, "arg-test", 1)
        val same = UiText.StringResource(1, "arg-test", 1)
        val differentArgs = UiText.StringResource(1, "other-arg-test", 1)
        val differentRes = UiText.StringResource(2, "arg-test", 1)

        assertEquals(first, same)
        assertEquals(first.hashCode(), same.hashCode())
        assertNotEquals(first, differentArgs)
        assertNotEquals(first, differentRes)
    }
}
