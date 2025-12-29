package kr.co.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.tasks.await
import kr.co.domain.exception.AuthException
import kr.co.domain.model.auth.User
import kr.co.domain.repository.AuthRepository
import javax.inject.Inject


class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    // 회원 가입
    override suspend fun createAccount(
        email: String,
        password: String,
        userName: String
    ): Result<User> =
        runCatching {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                user.updateProfile(userProfileChangeRequest { displayName = userName }).await()
                User(
                    email = user.email ?: "",
                    name = user.displayName ?: "",
                    photoUrl = user.photoUrl?.toString() ?: ""
                )
            } else {
                throw AuthException.CreateUserIsNullException
            }
        }

    // 회원 탈퇴
    override suspend fun deleteAccount(): Result<Unit> {
        val user = firebaseAuth.currentUser
        return if (user != null) {
            runCatching {
                user.delete().await()
            }
        } else {
            Result.failure(AuthException.CurrentUserIsNullException)
        }
    }

    // 로그인
    override suspend fun signIn(email: String, password: String): Result<User> = runCatching {
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val user = authResult.user
        if (user != null) {
            User(
                email = user.email ?: "",
                name = user.displayName ?: "",
                photoUrl = user.photoUrl.toString() ?: ""
            )
        } else {
            throw AuthException.SignInUserIsNullException
        }
    }

    // 로그아웃
    override suspend fun signOut(): Result<Unit> = runCatching {
        firebaseAuth.signOut()
    }

    // 로그인 유무 확인
    override suspend fun getCurrentUser(): Result<User> {
        val user = firebaseAuth.currentUser
        return if (user != null) {
            Result.success(
                User(
                    email = user.email ?: "",
                    name = user.displayName ?: "",
                    photoUrl = user.photoUrl.toString() ?: ""
                )
            )
        } else {
            Result.failure(AuthException.CurrentUserIsNullException)
        }
    }

    // 회원 프로필 업데이트
    override suspend fun updateProfile(userName: String, userPhotoUri: String): Result<Unit> {
        val user = firebaseAuth.currentUser
        return if (user != null) {
            runCatching {
                user.updateProfile(
                    userProfileChangeRequest {
                        displayName = userName
                        photoUri = Uri.parse(userPhotoUri)
                    }
                ).await()
            }
        } else {
            Result.failure(AuthException.CurrentUserIsNullException)
        }
    }
}