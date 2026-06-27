package kr.co.domain.testing

import com.github.michaelbull.result.Ok
import kr.co.core.common.result.AppResult
import kr.co.domain.service.time.ServerTimeProvider

class FakeServerTimeProvider(
    var currentTime: Long = DomainFixtures.FIXED_TIME,
    var syncResult: AppResult<Unit> = Ok(Unit),
) : ServerTimeProvider {
    var syncCallCount = 0
        private set

    override suspend fun sync(): AppResult<Unit> {
        syncCallCount += 1
        return syncResult
    }

    override fun now(): Long = currentTime
}
