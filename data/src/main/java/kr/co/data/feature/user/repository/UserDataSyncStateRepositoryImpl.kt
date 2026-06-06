package kr.co.data.feature.user.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.co.data.local.provider.UserSyncDataStoreProvider
import kr.co.domain.common.state.SyncProcessState
import kr.co.domain.feature.user.repository.UserDataSyncStateRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataSyncStateRepositoryImpl @Inject constructor(
    private val userSyncDataStoreProvider: UserSyncDataStoreProvider,
) : UserDataSyncStateRepository {

    private val dataStore get() = userSyncDataStoreProvider.getDataStore()

    private val _userDataSyncState = MutableStateFlow<SyncProcessState>(SyncProcessState.Idle)
    override val userDataSyncState: StateFlow<SyncProcessState> = _userDataSyncState.asStateFlow()

    override fun updateUserDataSyncState(state: SyncProcessState) {
        _userDataSyncState.value = state
    }

    override suspend fun getLastSyncTimestamp(): Long = dataStore.getLastUserDataSyncTimestamp()

    override suspend fun setLastSyncTimestamp(timestamp: Long) {
        dataStore.setLastUserDataSyncTimestamp(timestamp)
    }
}