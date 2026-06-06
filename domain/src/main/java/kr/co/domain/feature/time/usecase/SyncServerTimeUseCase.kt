package kr.co.domain.feature.time.usecase

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onErr
import kr.co.domain.error.DomainError
import kr.co.domain.feature.time.service.ServerTimeProvider
import kr.co.domain.feature.time.service.ServerTimeSyncScheduler
import javax.inject.Inject


class SyncServerTimeUseCase @Inject constructor(
    private val serverTime: ServerTimeProvider,
    private val serverTimeSyncScheduler: ServerTimeSyncScheduler,
) {
    suspend operator fun invoke(): Result<Unit, DomainError> {
        return serverTime.sync().onErr { serverTimeSyncScheduler.scheduleSync() }
    }
}