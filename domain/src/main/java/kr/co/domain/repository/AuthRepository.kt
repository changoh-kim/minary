package kr.co.domain.repository

import kr.co.domain.model.User

interface AuthRepository {
    suspend fun createAccount(email: String, password: String, userName: String): Result<User>
    suspend fun deleteAccount(): Result<Unit>
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): Result<User>
    suspend fun updateProfile(userName: String, userPhotoUri: String): Result<Unit>
}