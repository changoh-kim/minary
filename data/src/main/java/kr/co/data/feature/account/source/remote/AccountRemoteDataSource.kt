package kr.co.data.feature.account.source.remote

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import kr.co.core.firebase.provider.FirebaseAuthProvider
import kr.co.core.firebase.provider.FirebaseFunctionsProvider
import kr.co.data.feature.account.exception.AccountException
import kr.co.data.feature.account.model.AccountModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRemoteDataSource @Inject constructor(
    private val firebaseAuthProvider: FirebaseAuthProvider,
    private val firebaseFunctionsProvider: FirebaseFunctionsProvider,
) {
    companion object {
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "password"
        const val KEY_NAME = "name"
        const val KEY_GENDER = "gender"
        const val KEY_BIRTHDAY = "birthday"
        const val KEY_ADDRESS = "address"
        const val KEY_PHONE_NUMBER = "phoneNumber"
        const val KEY_JOINED_AT = "joinedAt"
    }

    suspend fun createAccount(
        email: String,
        password: String,
        name: String,
        gender: String,
        birthday: String,
        address: String,
        phoneNumber: String,
        joinedAt: Long,
    ) {
        require(email.isNotBlank()) { "Failed to create account with profile: Email is blank." }
        require(password.isNotBlank()) { "Failed to create account with profile: Password is blank." }
        require(name.isNotBlank()) { "Failed to create account with profile: name is blank." }

        val data = hashMapOf(
            KEY_EMAIL to email,
            KEY_PASSWORD to password,
            KEY_NAME to name,
            KEY_GENDER to gender,
            KEY_BIRTHDAY to birthday,
            KEY_ADDRESS to address,
            KEY_PHONE_NUMBER to phoneNumber,
            KEY_JOINED_AT to joinedAt
        )

        firebaseFunctionsProvider
            .getCreateAccountAndUserDataCallable()
            .call(data)
            .await()
    }

    suspend fun deleteAccount(password: String): String {
        val firebaseUser = firebaseAuthProvider.currentUser
            ?: throw AccountException.UserNotFoundException()

        try {
            // 1. 세션 유효성 확인
            firebaseUser.reload().await()
        } catch (_: FirebaseAuthInvalidUserException) {
            throw AccountException.SessionExpiredException()
        }
        // 2. 재인증 (비밀번호 확인 및 토큰 갱신)
        val currentUser =
            firebaseAuthProvider.currentUser ?: throw AccountException.SessionExpiredException()
        val email = currentUser.email ?: throw AccountException.UserNotFoundException()

        reauthenticate(currentUser, email, password)

        // 3. 서버 데이터 및 계정 삭제 수행
        firebaseFunctionsProvider
            .getDeleteAccountAndUserDataCallable()
            .call()
            .await()

        // 4. [추가] 클라이언트 세션 명시적 정리
        firebaseAuthProvider.signOut()

        return currentUser.uid
    }

    private suspend fun reauthenticate(user: FirebaseUser, email: String, password: String) {
        require(email.isNotBlank()) { "Failed to reauthenticate: Email is blank." }
        require(password.isNotBlank()) { "Failed to reauthenticate: Password is blank." }

        val credential = EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential).await()
    }

    suspend fun signIn(email: String, password: String): AccountModel {
        require(email.isNotBlank()) { "Failed to sign in: Email is blank." }
        require(password.isNotBlank()) { "Failed to sign in: Password is blank." }

        val result = firebaseAuthProvider.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user
            ?: throw AccountException.UserNotFoundException()

        return AccountModel(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
        )
    }

    fun signOut() = firebaseAuthProvider.signOut()

    suspend fun checkEmailAvailability(email: String): Boolean {
        require(email.isNotBlank()) { "Failed to check email availability: Email is blank." }

        val data = hashMapOf(KEY_EMAIL to email)
        val result = firebaseFunctionsProvider
            .getCheckEmailAvailabilityCallable()
            .call(data)
            .await()

        return ((result.data as? Map<*, *>)?.get("isAvailable") as? Boolean) ?: false
    }
}
