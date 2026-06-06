package kr.co.domain.feature.profile.usecase.sync

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kr.co.domain.error.DomainError
import kr.co.domain.feature.profile.service.sync.UserProfileRealtimeSyncScheduler
import javax.inject.Inject


class StartRealtimeUserProfileSyncUseCase @Inject constructor(
    private val userProfileRealtimeSyncScheduler: UserProfileRealtimeSyncScheduler,
) {
    suspend operator fun invoke(): Result<Unit, DomainError> {
        userProfileRealtimeSyncScheduler.startListening()
        return Ok(Unit)
    }
}