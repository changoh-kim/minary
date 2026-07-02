package kr.co.data.extension

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code as FirestoreCode
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.FirebaseFunctionsException.Code as FunctionsCode
import com.google.firebase.storage.StorageException
import io.mockk.every
import io.mockk.mockk
import kr.co.core.common.error.DomainError
import kr.co.data.feature.account.exception.AccountException
import kr.co.data.testing.BaseDataUnitTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ExceptionMapperTest : BaseDataUnitTest() {
    @Test
    fun `maps firebase network exception to network unavailable`() {
        val actual = mockk<FirebaseNetworkException>().toDomainError()

        assertEquals(DomainError.NetworkUnavailable, actual)
    }

    @Test
    fun `maps firebase too many requests exception to auth too many requests`() {
        val actual = mockk<FirebaseTooManyRequestsException>().toDomainError()

        assertEquals(DomainError.Auth.TooManyRequests, actual)
    }

    @Test
    fun `maps account user not found exception to auth user not found`() {
        val actual = AccountException.UserNotFoundException().toDomainError()

        assertEquals(DomainError.Auth.UserNotFound, actual)
    }

    @Test
    fun `maps account session expired exception to auth token expired`() {
        val actual = AccountException.SessionExpiredException().toDomainError()

        assertEquals(DomainError.Auth.TokenExpired, actual)
    }

    @Test
    fun `maps weak password exception to auth weak password`() {
        val actual = mockk<FirebaseAuthWeakPasswordException>().toDomainError()

        assertEquals(DomainError.Auth.WeakPassword, actual)
    }

    @Test
    fun `maps invalid credentials exception to auth invalid credentials`() {
        val actual = mockk<FirebaseAuthInvalidCredentialsException>().toDomainError()

        assertEquals(DomainError.Auth.InvalidCredentials, actual)
    }

    @Test
    fun `maps user collision exception to auth email already in use`() {
        val actual = mockk<FirebaseAuthUserCollisionException>().toDomainError()

        assertEquals(DomainError.Auth.EmailAlreadyInUse, actual)
    }

    @Test
    fun `maps recent login required exception to auth requires recent login`() {
        val actual = mockk<FirebaseAuthRecentLoginRequiredException>().toDomainError()

        assertEquals(DomainError.Auth.RequiresRecentLogin, actual)
    }

    @Test
    fun `maps firebase invalid user error codes`() {
        val cases = listOf(
            "ERROR_USER_NOT_FOUND" to DomainError.Auth.UserNotFound,
            "ERROR_USER_DISABLED" to DomainError.Auth.UserDisabled,
            "ERROR_USER_TOKEN_EXPIRED" to DomainError.Auth.TokenExpired,
            "ERROR_UNKNOWN_TEST" to DomainError.Unexpected,
        )

        cases.forEach { (code, expected) ->
            val exception = mockk<FirebaseAuthInvalidUserException> {
                every { errorCode } returns code
            }
            val actual = exception.toDomainError()

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `maps firebase functions exception codes`() {
        val cases = listOf(
            FunctionsCode.ALREADY_EXISTS to DomainError.Auth.EmailAlreadyInUse,
            FunctionsCode.INVALID_ARGUMENT to DomainError.Auth.InvalidCredentials,
            FunctionsCode.PERMISSION_DENIED to DomainError.Store.PermissionDenied,
            FunctionsCode.UNAUTHENTICATED to DomainError.Store.Unauthenticated,
            FunctionsCode.NOT_FOUND to DomainError.Store.NotFound,
            FunctionsCode.RESOURCE_EXHAUSTED to DomainError.Store.QuotaExceeded,
            FunctionsCode.UNAVAILABLE to DomainError.NetworkUnavailable,
            FunctionsCode.DEADLINE_EXCEEDED to DomainError.Timeout,
            FunctionsCode.INTERNAL to DomainError.Unexpected,
        )

        cases.forEach { (functionsCode, expected) ->
            val exception = mockk<FirebaseFunctionsException> {
                every { code } returns functionsCode
            }
            val actual = exception.toDomainError()

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `maps firebase firestore exception codes`() {
        val cases = listOf(
            FirestoreCode.UNAVAILABLE to DomainError.NetworkUnavailable,
            FirestoreCode.DEADLINE_EXCEEDED to DomainError.Timeout,
            FirestoreCode.PERMISSION_DENIED to DomainError.Store.PermissionDenied,
            FirestoreCode.UNAUTHENTICATED to DomainError.Store.Unauthenticated,
            FirestoreCode.NOT_FOUND to DomainError.Store.NotFound,
            FirestoreCode.ALREADY_EXISTS to DomainError.Store.AlreadyExists,
            FirestoreCode.RESOURCE_EXHAUSTED to DomainError.Store.QuotaExceeded,
            FirestoreCode.INTERNAL to DomainError.Unexpected,
        )

        cases.forEach { (firestoreCode, expected) ->
            val exception = mockk<FirebaseFirestoreException> {
                every { code } returns firestoreCode
            }
            val actual = exception.toDomainError()

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `maps firebase storage exception codes`() {
        val cases = listOf(
            StorageException.ERROR_NOT_AUTHORIZED to DomainError.Storage.PermissionDenied,
            StorageException.ERROR_QUOTA_EXCEEDED to DomainError.Storage.QuotaExceeded,
            StorageException.ERROR_OBJECT_NOT_FOUND to DomainError.Storage.NotFound,
            StorageException.ERROR_RETRY_LIMIT_EXCEEDED to DomainError.NetworkUnavailable,
            StorageException.ERROR_UNKNOWN to DomainError.Unexpected,
        )

        cases.forEach { (code, expected) ->
            val exception = mockk<StorageException> {
                every { errorCode } returns code
            }
            val actual = exception.toDomainError()

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `maps unknown exception to unexpected`() {
        val actual = IllegalStateException("unknown-error-test").toDomainError()

        assertEquals(DomainError.Unexpected, actual)
    }
}
