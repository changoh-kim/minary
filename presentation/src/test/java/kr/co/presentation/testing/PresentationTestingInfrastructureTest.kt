package kr.co.presentation.testing

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PresentationTestingInfrastructureTest : BaseViewModelTest() {

    @Test
    fun `fixtures use synthetic non personal data`() = runPresentationTest {
        val account = PresentationFixtures.account()
        val diary = PresentationFixtures.diary()

        assertEquals("uid-test", account.uid)
        assertEquals("user-test@example.test", account.email)
        assertEquals("title-test", diary.title)
        assertEquals("content-test", diary.content)
    }

    @Test
    fun `turbine helper awaits matching state`() = runPresentationTest {
        val state = MutableStateFlow("initial")

        state.test {
            assertEquals("initial", awaitItem())
            state.value = "ready"

            assertEquals("ready", awaitState { it == "ready" })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
