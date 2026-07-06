package kr.co.presentation.testing

import app.cash.turbine.ReceiveTurbine
import io.mockk.clearAllMocks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

@OptIn(ExperimentalCoroutinesApi::class, OrbitExperimental::class)
abstract class BaseViewModelTest {
    protected val testDispatcher = StandardTestDispatcher()
    private val trackedContainers = mutableListOf<ContainerHost<*, *>>()

    @BeforeEach
    fun setUpCoroutine() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDownCoroutine() {
        runBlocking {
            cancelTrackedContainersAndJoin()
        }
        Dispatchers.resetMain()
        clearAllMocks()
    }

    protected fun runPresentationTest(block: suspend TestScope.() -> Unit) =
        runTest(testDispatcher) {
            try {
                block()
            } finally {
                cancelTrackedContainersAndJoin()
                advanceUntilIdle()
            }
        }

    protected fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.stateFlow(): StateFlow<STATE> =
        container.stateFlow

    protected fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.sideEffectFlow(): Flow<SIDE_EFFECT> =
        container.sideEffectFlow

    protected fun <STATE : Any, SIDE_EFFECT : Any, VIEW_MODEL> VIEW_MODEL.trackOrbitViewModel(): VIEW_MODEL
        where VIEW_MODEL : ContainerHost<STATE, SIDE_EFFECT> {
        trackedContainers += this
        return this
    }

    protected suspend fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.joinOrbitIntents() {
        container.joinIntents()
    }

    protected suspend fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.awaitState(
        predicate: (STATE) -> Boolean,
    ): STATE = withContext(Dispatchers.Default.limitedParallelism(1)) {
        withTimeout(DEFAULT_AWAIT_TIMEOUT_MILLIS) {
            while (true) {
                val state = container.stateFlow.value
                if (predicate(state)) return@withTimeout state
                delay(STATE_POLL_INTERVAL_MILLIS)
            }
            error("Unreachable")
        }
    }

    protected suspend fun <STATE> ReceiveTurbine<STATE>.awaitState(
        predicate: (STATE) -> Boolean,
    ): STATE {
        repeat(MAX_STATE_ITEMS_TO_SCAN) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
        error("Expected state was not emitted within $MAX_STATE_ITEMS_TO_SCAN items.")
    }

    protected suspend inline fun <reified SIDE_EFFECT : Any> ReceiveTurbine<out Any>.expectSideEffect(): SIDE_EFFECT {
        val item = awaitItem()
        return item as? SIDE_EFFECT
            ?: error("Expected ${SIDE_EFFECT::class.simpleName}, but was ${item::class.simpleName}.")
    }

    private suspend fun cancelTrackedContainersAndJoin() {
        val containers = trackedContainers.toList()
        trackedContainers.clear()
        containers.forEach { it.container.cancel() }
        containers.forEach { it.container.joinIntents() }
    }

    private companion object {
        private const val MAX_STATE_ITEMS_TO_SCAN = 20
        private const val DEFAULT_AWAIT_TIMEOUT_MILLIS = 3_000L
        private const val STATE_POLL_INTERVAL_MILLIS = 10L
    }
}
