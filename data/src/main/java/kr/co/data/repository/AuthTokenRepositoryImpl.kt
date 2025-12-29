package kr.co.data.repository

import kr.co.data.datastore.MinaryDataStore
import kr.co.domain.repository.AuthTokenRepository
import javax.inject.Inject


// FirebaseAuth 이전에 사용하던 구현체
class AuthTokenRepositoryImpl @Inject constructor(
    private val minaryDataStore: MinaryDataStore
) : AuthTokenRepository {

    override suspend fun setToken(token: String) {
        return minaryDataStore.setToken(token)
    }

    override suspend fun getToken(): String {
        return minaryDataStore.getToken()
    }

    override suspend fun clearToken() {
        return minaryDataStore.clearToken()
    }
}