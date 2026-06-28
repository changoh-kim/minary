package kr.co.core.storage.testing

import io.mockk.clearAllMocks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseUnitTest {
    protected val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUpCoroutine() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDownCoroutine() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    protected fun runCoreTest(block: suspend TestScope.() -> Unit) =
        runTest(testDispatcher) { block() }
}
