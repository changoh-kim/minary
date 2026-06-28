package kr.co.core.ui.common.load

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kr.co.core.common.error.DomainError
import org.junit.Rule
import org.junit.Test

class LoadStateContentContainerInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersContentBranchForEachLoadState() {
        composeRule.setContent {
            LoadStateContentContainer(
                loadState = LoadState.Success("value-test"),
                uninitialized = { Text("uninitialized-test") },
                loading = { Text("loading-test") },
                error = { Text("error-test") },
            ) { value ->
                Text(value)
            }
        }

        composeRule.onNodeWithText("value-test").assertExists()
    }

    @Test
    fun rendersErrorBranchWithDomainError() {
        composeRule.setContent {
            LoadStateContentContainer(
                loadState = LoadState.Error(DomainError.Timeout),
                error = { error -> Text(error.toString()) },
            ) {
                Text("content-test")
            }
        }

        composeRule.onNodeWithText(DomainError.Timeout.toString()).assertExists()
    }
}
