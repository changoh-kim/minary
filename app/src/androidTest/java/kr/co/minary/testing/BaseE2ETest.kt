package kr.co.minary.testing

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import kr.co.presentation.app.activity.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

abstract class BaseE2ETest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var fakeBackend: FakeAppBackend

    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun setUpE2E() {
        hiltRule.inject()
        fakeBackend.reset()
    }

    @After
    fun tearDownE2E() {
        scenario?.close()
        scenario = null
        fakeBackend.reset()
    }

    protected fun launchApp() {
        repeat(MAX_LAUNCH_ATTEMPTS) { attempt ->
            scenario = launchMainActivity()
            waitForActivityIdle()
            if (waitForComposeRoot()) {
                composeRule.waitForIdle()
                return
            }
            scenario?.close()
            scenario = null
            waitForActivityIdle()
        }

        error("Compose root was not registered after launching MainActivity.")
    }

    protected fun text(@StringRes resId: Int): String =
        ApplicationProvider.getApplicationContext<Context>().getString(resId)

    private fun launchMainActivity(): ActivityScenario<MainActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        return ActivityScenario.launch(intent)
    }

    private fun waitForActivityIdle() {
        scenario?.onActivity { }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun waitForComposeRoot(timeoutMillis: Long = 10_000): Boolean =
        runCatching {
            composeRule.waitUntil(timeoutMillis) {
                runCatching {
                    composeRule.onAllNodes(isRoot()).fetchSemanticsNodes().isNotEmpty()
                }.getOrDefault(false)
            }
        }.isSuccess

    private companion object {
        const val MAX_LAUNCH_ATTEMPTS = 3
    }
}
