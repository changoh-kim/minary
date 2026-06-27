package kr.co.domain.testing.fake

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kr.co.core.common.state.SyncProcessState
import kr.co.domain.feature.user.repository.UserDataSyncStateRepository

class FakeUserDataSyncStateRepository(
    initialState: SyncProcessState = SyncProcessState.Idle,
    initialLastSyncTimestamp: Long = 0L,
) : UserDataSyncStateRepository {
    private val state = MutableStateFlow(initialState)

    val stateUpdates = mutableListOf<SyncProcessState>()
    val savedTimestamps = mutableListOf<Long>()

    var lastSyncTimestamp: Long = initialLastSyncTimestamp

    override val userDataSyncState: StateFlow<SyncProcessState> = state

    override fun updateUserDataSyncState(state: SyncProcessState) {
        stateUpdates += state
        this.state.value = state
    }

    override suspend fun getLastSyncTimestamp(): Long = lastSyncTimestamp

    override suspend fun setLastSyncTimestamp(timestamp: Long) {
        savedTimestamps += timestamp
        lastSyncTimestamp = timestamp
    }
}
