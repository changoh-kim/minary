package kr.co.core.ui.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import kr.co.core.common.model.AppTheme
import org.junit.Rule
import org.junit.Test

class MinaryThemeInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersContentWithLightAndDarkTheme() {
        composeRule.setContent {
            MinaryTheme(appTheme = AppTheme.LIGHT) {
                Text(
                    text = MaterialTheme.colorScheme.primary.toString(),
                    modifier = Modifier.testTag("theme-content-test"),
                )
            }
        }
        composeRule.onNodeWithTag("theme-content-test").assertExists()

        composeRule.setContent {
            MinaryTheme(appTheme = AppTheme.DARK) {
                Text(
                    text = MaterialTheme.colorScheme.primary.toString(),
                    modifier = Modifier.testTag("theme-content-test"),
                )
            }
        }
        composeRule.onNodeWithTag("theme-content-test").assertExists()
    }
}
