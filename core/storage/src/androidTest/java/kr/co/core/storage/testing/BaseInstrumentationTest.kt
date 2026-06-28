package kr.co.core.storage.testing

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.clearAllMocks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseInstrumentationTest {
    protected val context: Context
        get() = ApplicationProvider.getApplicationContext()

    protected val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUpCoroutine() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDownCoroutine() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    protected fun runCoreAndroidTest(block: suspend TestScope.() -> Unit) =
        runTest(testDispatcher) { block() }
}
