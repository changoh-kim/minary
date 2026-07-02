package kr.co.data.service.time

import android.content.Context
import com.instacart.library.truetime.TrueTime
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kr.co.core.common.error.DomainError
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.assertErr
import kr.co.data.testing.assertOk
import kr.co.domain.service.time.ServerTimeSyncScheduler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.Date

class TrueTimeProviderTest : BaseDataUnitTest() {
    @Test
    fun `sync initializes TrueTime with project NTP settings`() = runDataTest {
        withMockedTrueTimeBuilder { trueTime ->
            val context = mockk<Context>()
            every { trueTime.withNtpHost("time.google.com") } returns trueTime
            every { trueTime.withConnectionTimeout(10_000) } returns trueTime
            every { trueTime.withSharedPreferencesCache(context) } returns trueTime
            every { trueTime.withLoggingEnabled(true) } returns trueTime
            every { trueTime.initialize() } just Runs
            val provider = provider(context = context)

            provider.sync().assertOk(Unit)

            verify(exactly = 1) { trueTime.withNtpHost("time.google.com") }
            verify(exactly = 1) { trueTime.withConnectionTimeout(10_000) }
            verify(exactly = 1) { trueTime.withSharedPreferencesCache(context) }
            verify(exactly = 1) { trueTime.withLoggingEnabled(true) }
            verify(exactly = 1) { trueTime.initialize() }
        }
    }

    @Test
    fun `sync maps TrueTime initialization failure to unexpected`() = runDataTest {
        withMockedTrueTimeBuilder { trueTime ->
            val context = mockk<Context>()
            every { trueTime.withNtpHost(any()) } returns trueTime
            every { trueTime.withConnectionTimeout(any()) } returns trueTime
            every { trueTime.withSharedPreferencesCache(context) } returns trueTime
            every { trueTime.withLoggingEnabled(any()) } returns trueTime
            every { trueTime.initialize() } throws IOException("failure-test")
            val provider = provider(context = context)

            provider.sync().assertErr(DomainError.Unexpected)
        }
    }

    @Test
    fun `now returns TrueTime value without scheduling sync when initialized`() {
        withMockedTrueTime {
            val scheduler = mockk<ServerTimeSyncScheduler>(relaxed = true)
            every { TrueTime.isInitialized() } returns true
            every { TrueTime.now() } returns Date(SERVER_NOW)
            val provider = provider(syncScheduler = scheduler)

            assertEquals(SERVER_NOW, provider.now())

            verify(exactly = 0) { scheduler.scheduleSync() }
        }
    }

    @Test
    fun `now schedules sync and returns device time fallback when TrueTime is not initialized`() {
        withMockedTrueTime {
            val scheduler = mockk<ServerTimeSyncScheduler>(relaxed = true)
            every { TrueTime.isInitialized() } returns false
            val provider = provider(syncScheduler = scheduler)

            val before = System.currentTimeMillis()
            val actual = provider.now()
            val after = System.currentTimeMillis()

            assertTrue(actual in before..after)
            verify(exactly = 1) { scheduler.scheduleSync() }
        }
    }

    private fun provider(
        context: Context = mockk(),
        syncScheduler: ServerTimeSyncScheduler = mockk(relaxed = true),
    ) = TrueTimeProvider(
        context = context,
        serverTimeSyncScheduler = syncScheduler,
    )

    private inline fun withMockedTrueTime(block: () -> Unit) {
        mockkStatic(TrueTime::class)
        try {
            block()
        } finally {
            unmockkStatic(TrueTime::class)
        }
    }

    private inline fun withMockedTrueTimeBuilder(block: (TrueTime) -> Unit) {
        withMockedTrueTime {
            val trueTime = mockk<TrueTime>()
            every { TrueTime.build() } returns trueTime
            block(trueTime)
        }
    }

    private companion object {
        const val SERVER_NOW = 123_456L
    }
}
