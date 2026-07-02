package kr.co.data.service.time.sync.worker

import androidx.work.ListenableWorker
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kr.co.core.common.error.DomainError
import kr.co.core.common.result.AppResult
import kr.co.data.testing.BaseDataInstrumentationTest
import kr.co.data.testing.workerParameters
import kr.co.domain.service.time.ServerTimeProvider
import org.junit.Assert.assertEquals
import org.junit.Test

class ServerTimeSyncWorkerInstrumentedTest : BaseDataInstrumentationTest() {
    @Test
    fun doWork_returns_success_when_server_time_sync_succeeds() = runDataAndroidTest {
        val serverTime = FakeServerTimeProvider(syncResult = Ok(Unit))
        val worker = worker(serverTime = serverTime)

        assertEquals(ListenableWorker.Result.success(), worker.doWork())
    }

    @Test
    fun doWork_returns_retry_when_sync_fails_before_retry_limit() = runDataAndroidTest {
        val serverTime = FakeServerTimeProvider(syncResult = Err(DomainError.NetworkUnavailable))
        val worker = worker(serverTime = serverTime, runAttemptCount = 2)

        assertEquals(ListenableWorker.Result.retry(), worker.doWork())
    }

    @Test
    fun doWork_returns_failure_when_sync_fails_at_retry_limit() = runDataAndroidTest {
        val serverTime = FakeServerTimeProvider(syncResult = Err(DomainError.NetworkUnavailable))
        val worker = worker(serverTime = serverTime, runAttemptCount = 3)

        assertEquals(ListenableWorker.Result.failure(), worker.doWork())
    }

    private fun worker(
        serverTime: ServerTimeProvider,
        runAttemptCount: Int = 0,
    ) = ServerTimeSyncWorker(
        context = context,
        params = workerParameters(runAttemptCount),
        serverTime = serverTime,
    )

    private class FakeServerTimeProvider(
        private val syncResult: AppResult<Unit>,
    ) : ServerTimeProvider {
        override suspend fun sync(): AppResult<Unit> = syncResult

        override fun now(): Long = 0L
    }
}
