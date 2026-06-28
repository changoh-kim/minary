package kr.co.core.ui.common.text

import kr.co.core.ui.common.R
import kr.co.core.ui.common.testing.BaseInstrumentationTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UiTextExtensionsInstrumentedTest : BaseInstrumentationTest() {

    @Test
    fun contextGetStringResolvesDynamicAndResourceText() {
        assertEquals("value-test", context.getString(UiText.DynamicString("value-test")))
        assertEquals(
            context.getString(R.string.emotion_joy),
            context.getString(UiText.StringResource(R.string.emotion_joy)),
        )
    }
}
