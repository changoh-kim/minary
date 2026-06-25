package kr.co.data.feature.session.source.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kr.co.core.firebase.provider.FirebaseAuthProvider
import kr.co.data.feature.account.exception.AccountException
import kr.co.data.feature.session.model.UserSessionModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRemoteDataSource @Inject constructor(
    private val firebaseAuthProvider: FirebaseAuthProvider,
) {
    /**
     * 로컬 세션에 저장된 사용자 정보를 반환합니다.
     */
    fun getCurrentUser(): UserSessionModel {
        val currentUser = firebaseAuthProvider.currentUser ?: throw AccountException.UserNotFoundException()
        return UserSessionModel(
            uid = currentUser.uid,
            email = currentUser.email ?: "",
        )
    }

    /**
     * 서버에서 최신 사용자 정보를 받아옵니다. 받아온 정보는 로컬 세션에 저장됩니다.
     */
    suspend fun reload(): UserSessionModel {
        val firebaseUser = firebaseAuthProvider.currentUser
            ?: throw AccountException.UserNotFoundException()

        try {
            firebaseUser.reload().await()
        } catch (_: FirebaseAuthInvalidUserException) {
            throw AccountException.SessionExpiredException()
        }

        val currentUser = firebaseAuthProvider.currentUser
            ?: throw AccountException.SessionExpiredException()

        return UserSessionModel(
            uid = currentUser.uid,
            email = currentUser.email ?: "",
        )
    }

    fun observeSessionStateFlow(): Flow<UserSessionModel?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                trySend(
                    UserSessionModel(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                    )
                )
            } else {
                trySend(null)
            }
        }
        firebaseAuthProvider.addAuthStateListener(listener)
        awaitClose {
            firebaseAuthProvider.removeAuthStateListener(listener)
        }
    }
}
