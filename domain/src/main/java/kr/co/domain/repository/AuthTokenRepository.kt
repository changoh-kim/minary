package kr.co.domain.repository


interface AuthTokenRepository {
    suspend fun setToken(token: String)
    suspend fun getToken() : String
    suspend fun clearToken()
}