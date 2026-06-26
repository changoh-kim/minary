package kr.co.domain.feature.time.usecase

import kr.co.core.common.result.AppResult
import com.github.michaelbull.result.onErr
import kr.co.domain.service.time.ServerTimeProvider
import kr.co.domain.service.time.ServerTimeSyncScheduler
import javax.inject.Inject


class SyncServerTimeUseCase @Inject constructor(
    private val serverTime: ServerTimeProvider,
    private val serverTimeSyncScheduler: ServerTimeSyncScheduler,
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return serverTime.sync().onErr { serverTimeSyncScheduler.scheduleSync() }
    }
}