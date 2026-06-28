package kr.co.core.ui.common.resource

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kr.co.core.ui.common.test.R
import org.junit.Rule
import org.junit.Test

class StringArrayResourceInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun readsStringArrayResourceInsideComposable() {
        composeRule.setContent {
            Text(stringArrayResource(R.array.test_values).joinToString(","))
        }

        composeRule.onNodeWithText("first-test,second-test").assertExists()
    }
}
