package kr.co.domain.feature.diary.repository.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kr.co.domain.common.state.SyncProcessState

interface DiarySyncStateRepository {
    val initDiarySyncState: StateFlow<SyncProcessState>
    val pendingCount: Flow<Int>

    fun updateInitDiarySyncState(state: SyncProcessState)
    suspend fun isInitialSyncCompleted(): Boolean
    suspend fun setInitialSyncCompleted(completed: Boolean)
}