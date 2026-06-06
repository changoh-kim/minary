package kr.co.data.feature.diary.repository.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.co.data.local.provider.UserDatabaseProvider
import kr.co.data.local.provider.UserSyncDataStoreProvider
import kr.co.domain.common.state.SyncProcessState
import kr.co.domain.feature.diary.repository.sync.DiarySyncStateRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiarySyncStateRepositoryImpl @Inject constructor(
    private val userDatabaseProvider: UserDatabaseProvider,
    private val userSyncDataStoreProvider: UserSyncDataStoreProvider,
) : DiarySyncStateRepository {

    private val diaryDao get() = userDatabaseProvider.getDatabase().diaryDao()
    private val dataStore get() = userSyncDataStoreProvider.getDataStore()

    private val _initDiarySyncState =
        MutableStateFlow<SyncProcessState>(SyncProcessState.Idle)

    override val initDiarySyncState: StateFlow<SyncProcessState> =
        _initDiarySyncState.asStateFlow()

    override val pendingCount: Flow<Int>
        get() = diaryDao.getPendingItemCountFlow()

    override fun updateInitDiarySyncState(state: SyncProcessState) {
        _initDiarySyncState.value = state
    }

    override suspend fun isInitialSyncCompleted(): Boolean =
        dataStore.isInitialSyncCompleted()

    override suspend fun setInitialSyncCompleted(completed: Boolean) {
        dataStore.setInitialSyncCompleted(completed)
    }
}