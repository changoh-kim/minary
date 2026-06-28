package kr.co.core.ui.design.component

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LoadingIconButtonInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun enabledIconButtonHandlesClick() {
        var clickCount = 0
        composeRule.setContent {
            LoadingIconButton(
                onClick = { clickCount += 1 },
                isLoading = false,
            ) { isLoading ->
                Text(if (isLoading) "loading-icon-test" else "ready-icon-test")
            }
        }

        composeRule.onNodeWithText("ready-icon-test").assertIsEnabled().performClick()
        assertEquals(1, clickCount)
    }

    @Test
    fun loadingIconButtonIsDisabledAndPassesLoadingStateToContent() {
        composeRule.setContent {
            LoadingIconButton(
                onClick = {},
                isLoading = true,
            ) { isLoading ->
                Text(if (isLoading) "loading-icon-test" else "ready-icon-test")
            }
        }

        composeRule.onNodeWithText("loading-icon-test").assertIsNotEnabled()
    }
}
