package kr.co.domain.testing.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kr.co.core.common.state.SyncProcessState
import kr.co.domain.feature.diary.sync.DiarySyncStateRepository

class FakeDiarySyncStateRepository(
    initialState: SyncProcessState = SyncProcessState.Idle,
    initialPendingCount: Int = 0,
    initialSyncCompleted: Boolean = false,
) : DiarySyncStateRepository {
    private val state = MutableStateFlow(initialState)
    private val pendingCountState = MutableStateFlow(initialPendingCount)

    val stateUpdates = mutableListOf<SyncProcessState>()
    val setInitialSyncCompletedCalls = mutableListOf<Boolean>()

    var initialSyncCompleted: Boolean = initialSyncCompleted

    override val initDiarySyncState: StateFlow<SyncProcessState> = state
    override val pendingCount: Flow<Int> = pendingCountState

    fun setPendingCount(count: Int) {
        pendingCountState.value = count
    }

    override fun updateInitDiarySyncState(state: SyncProcessState) {
        stateUpdates += state
        this.state.value = state
    }

    override suspend fun isInitialSyncCompleted(): Boolean = initialSyncCompleted

    override suspend fun setInitialSyncCompleted(completed: Boolean) {
        setInitialSyncCompletedCalls += completed
        initialSyncCompleted = completed
    }
}
