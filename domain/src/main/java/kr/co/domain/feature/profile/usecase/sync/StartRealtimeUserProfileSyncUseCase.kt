package kr.co.domain.feature.profile.usecase.sync

import com.github.michaelbull.result.Ok
import kr.co.core.common.result.AppResult
import kr.co.domain.feature.profile.sync.UserProfileRealtimeSyncScheduler
import javax.inject.Inject


class StartRealtimeUserProfileSyncUseCase @Inject constructor(
    private val userProfileRealtimeSyncScheduler: UserProfileRealtimeSyncScheduler,
) {
    suspend operator fun invoke(): AppResult<Unit> {
        userProfileRealtimeSyncScheduler.startListening()
        return Ok(Unit)
    }
}