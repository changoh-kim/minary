package kr.co.presentation.testing

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kr.co.core.ui.design.theme.MinaryTheme
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
abstract class BaseComposeTest {
    @Rule
    @JvmField
    val composeRule = createAndroidComposeRule<ComposeTestActivity>()

    protected fun setMinaryContent(
        content: @Composable () -> Unit,
    ) {
        composeRule.setContent {
            MinaryTheme {
                content()
            }
        }
    }

    protected fun composeRule(): ComposeContentTestRule =
        composeRule

    protected fun text(
        @StringRes resId: Int,
        vararg formatArgs: Any,
    ): String = InstrumentationRegistry.getInstrumentation()
        .targetContext
        .getString(resId, *formatArgs)
}
