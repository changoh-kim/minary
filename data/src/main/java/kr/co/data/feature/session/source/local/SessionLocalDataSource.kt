package kr.co.data.feature.session.source.local

import kr.co.data.local.datastore.MinaryPrefsDataStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionLocalDataSource @Inject constructor(
    private val appDataStore: MinaryPrefsDataStore,
) {
    suspend fun getLastSignInUid(): String? = appDataStore.getLastSignInUid()
    suspend fun setLastSignInUid(uid: String) = appDataStore.setLastSignInUid(uid)
}