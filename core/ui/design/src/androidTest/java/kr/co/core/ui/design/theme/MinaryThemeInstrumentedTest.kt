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
    fun rendersContentWithLightTheme() {
        renderTheme(AppTheme.LIGHT)

        composeRule.onNodeWithTag("theme-content-test").assertExists()
    }

    @Test
    fun rendersContentWithDarkTheme() {
        renderTheme(AppTheme.DARK)

        composeRule.onNodeWithTag("theme-content-test").assertExists()
    }

    private fun renderTheme(appTheme: AppTheme) {
        composeRule.setContent {
            MinaryTheme(appTheme = appTheme) {
                Text(
                    text = MaterialTheme.colorScheme.primary.toString(),
                    modifier = Modifier.testTag("theme-content-test"),
                )
            }
        }
    }
}
