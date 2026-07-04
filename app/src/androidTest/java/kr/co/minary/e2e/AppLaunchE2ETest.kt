package kr.co.minary.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.HiltAndroidTest
import kr.co.minary.testing.BaseE2ETest
import org.junit.Test
import kr.co.presentation.R as PresentationR

@LargeTest
@HiltAndroidTest
class AppLaunchE2ETest : BaseE2ETest() {

    @Test
    fun signedOutUser_startsAtWelcome() {
        launchApp()

        composeRule
            .onNodeWithText(text(PresentationR.string.welcome_title))
            .assertIsDisplayed()
    }

    @Test
    fun signedInUser_startsAtHome() {
        fakeBackend.signInAsTestUser()

        launchApp()

        composeRule
            .onNodeWithText(text(PresentationR.string.calendar).uppercase())
            .assertIsDisplayed()
    }

    @Test
    fun maintenanceMode_blocksMainFlow() {
        fakeBackend.setMaintenance("maintenance-test")

        launchApp()

        composeRule
            .onNodeWithText(text(PresentationR.string.system_maintenance_title))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText("maintenance-test")
            .assertIsDisplayed()
    }
}
