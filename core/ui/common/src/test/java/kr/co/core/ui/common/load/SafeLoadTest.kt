package kr.co.core.ui.common.load

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.CancellationException
import kr.co.core.common.error.DomainError
import kr.co.core.ui.common.testing.BaseUnitTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SafeLoadTest : BaseUnitTest() {

    @Test
    fun `emits loading then success load state and callbacks in order`() {
        runCoreTest {
            val events = mutableListOf<String>()
            val states = mutableListOf<LoadState<String>>()

            load { Ok("value-test") }
                .map { "$it-mapped" }
                .onStart { events += "start" }
                .onLoading { events += "loading-$it" }
                .onFinally { events += "finally" }
                .startAsLoadState { states += it }

            assertEquals(
                listOf("start", "loading-true", "loading-false", "finally"),
                events,
            )
            assertEquals(
                listOf(LoadState.Loading, LoadState.Success("value-test-mapped")),
                states,
            )
        }
    }

    @Test
    fun `emits error load state for app result failure`() {
        runCoreTest {
            val errors = mutableListOf<DomainError>()
            val states = mutableListOf<LoadState<String>>()

            load<String> { Err(DomainError.NetworkUnavailable) }
                .onError { errors += it }
                .startAsLoadState { states += it }

            assertEquals(
                listOf(LoadState.Loading, LoadState.Error(DomainError.NetworkUnavailable)),
                states,
            )
            assertEquals(listOf(DomainError.NetworkUnavailable), errors)
        }
    }

    @Test
    fun `maps unexpected exception to unexpected domain error`() {
        runCoreTest {
            val errors = mutableListOf<DomainError>()

            load<String> { throw IllegalStateException("failure-test") }
                .onError { errors += it }
                .start()

            assertEquals(listOf(DomainError.Unexpected), errors)
        }
    }

    @Test
    fun `rethrows cancellation exception`() {
        assertThrows(CancellationException::class.java) {
            runCoreTest {
                load<String> { throw CancellationException("cancel-test") }.start()
            }
        }
    }
}
