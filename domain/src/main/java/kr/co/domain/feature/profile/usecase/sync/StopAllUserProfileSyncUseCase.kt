package kr.co.domain.feature.profile.usecase.sync

import kr.co.domain.feature.profile.service.sync.UserProfileSyncScheduler
import javax.inject.Inject

class StopAllUserProfileSyncUseCase @Inject constructor(
    private val userProfileSyncScheduler: UserProfileSyncScheduler,
    private val stopRealtimeUserProfileSync: StopRealtimeUserProfileSyncUseCase,
) {
    operator fun invoke() {
        userProfileSyncScheduler.cancelAll()
        stopRealtimeUserProfileSync()
    }
}