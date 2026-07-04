package kr.co.presentation.testing

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import org.junit.Test

class PresentationComposeInfrastructureTest : BaseComposeTest() {

    @Test
    fun setMinaryContent_renders_content_inside_theme() {
        setMinaryContent {
            Text("content-test")
        }

        composeRule.onNodeWithText("content-test").assertIsDisplayed()
    }
}
