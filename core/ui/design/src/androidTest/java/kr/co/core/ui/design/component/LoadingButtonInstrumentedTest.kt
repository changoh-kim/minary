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

class LoadingButtonInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun enabledButtonHandlesClickAndRendersContent() {
        var clickCount = 0
        composeRule.setContent {
            LoadingButton(
                text = "Save",
                onClick = { clickCount += 1 },
                isLoading = false,
                content = { Text("content-test") },
            )
        }

        composeRule.onNodeWithText("Save").assertIsEnabled().performClick()
        composeRule.onNodeWithText("content-test").assertExists()
        assertEquals(1, clickCount)
    }

    @Test
    fun loadingButtonIsDisabledAndHidesTrailingContent() {
        composeRule.setContent {
            LoadingButton(
                text = "Save",
                onClick = {},
                isLoading = true,
                content = { Text("content-test") },
            )
        }

        composeRule.onNodeWithText("Save").assertIsNotEnabled()
        composeRule.onNodeWithText("content-test").assertDoesNotExist()
    }
}
