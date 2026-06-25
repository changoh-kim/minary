package kr.co.domain.feature.profile.usecase.sync

import kr.co.domain.feature.profile.sync.UserProfileRealtimeSyncScheduler
import javax.inject.Inject


class StopRealtimeUserProfileSyncUseCase @Inject constructor(
    private val userProfileRealtimeSyncScheduler: UserProfileRealtimeSyncScheduler,
) {
    operator fun invoke(): Unit = userProfileRealtimeSyncScheduler.stopListening()
}