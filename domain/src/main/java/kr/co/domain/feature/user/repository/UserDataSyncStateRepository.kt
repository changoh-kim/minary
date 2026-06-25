package kr.co.domain.feature.user.repository

import kotlinx.coroutines.flow.StateFlow
import kr.co.core.common.state.SyncProcessState

interface UserDataSyncStateRepository {
    val userDataSyncState: StateFlow<SyncProcessState>

    fun updateUserDataSyncState(state: SyncProcessState)

    suspend fun getLastSyncTimestamp(): Long
    suspend fun setLastSyncTimestamp(timestamp: Long)
}