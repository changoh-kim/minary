package kr.co.domain.feature.user.usecase

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kr.co.core.common.state.SyncProcessState
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.profile.sync.UserProfileSyncManager
import kr.co.domain.feature.setting.sync.UserSettingsSyncManager
import kr.co.domain.service.time.ServerTimeProvider
import kr.co.domain.feature.user.repository.UserDataSyncStateRepository
import javax.inject.Inject

class StartUserDataSyncUseCase @Inject constructor(
    private val userProfileSyncManager: UserProfileSyncManager,
    private val userSettingsSyncManager: UserSettingsSyncManager,
    private val userDataSyncStateRepository: UserDataSyncStateRepository,
    private val serverTime: ServerTimeProvider,
) {
    private companion object {
        const val SYNC_INTERVAL = 1000 * 60 * 60L // 1시간
    }

    suspend operator fun invoke(currentUid: String): Result<Unit, DomainError> = coroutineBinding {
        val lastSyncTimestamp = userDataSyncStateRepository.getLastSyncTimestamp()
        val currentTimestamp = serverTime.now()
        // 1시간 이내 사용자 데이터 동기화를 했었다면 중지
        if (currentTimestamp - lastSyncTimestamp < SYNC_INTERVAL) {
            return@coroutineBinding
        }

        userDataSyncStateRepository.updateUserDataSyncState(SyncProcessState.InProgress.Indeterminate)
        val (profileResult, settingsResult) = coroutineScope {
            val profileDeferred = async { userProfileSyncManager.syncProfile(currentUid) }
            val settingsDeferred = async { userSettingsSyncManager.syncSettings(currentUid) }
            profileDeferred.await() to settingsDeferred.await()
        }

        profileResult.bind()
        settingsResult.bind()

        userDataSyncStateRepository.setLastSyncTimestamp(currentTimestamp)
    }.onOk {
        userDataSyncStateRepository.updateUserDataSyncState(SyncProcessState.Completed)
    }.onErr {
        userDataSyncStateRepository.updateUserDataSyncState(SyncProcessState.Failed(it))
    }
}