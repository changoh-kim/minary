package kr.co.data.feature.account.source.remote

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.functions.HttpsCallableReference
import com.google.firebase.functions.HttpsCallableResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import kr.co.core.firebase.provider.FirebaseAuthProvider
import kr.co.core.firebase.provider.FirebaseFunctionsProvider
import kr.co.data.feature.account.exception.AccountException
import kr.co.data.feature.account.model.AccountModel
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class AccountRemoteDataSourceTest : BaseDataUnitTest() {
    @Test
    fun `createAccount sends profile payload to callable`() = runDataTest {
        val payloadSlot = slot<Any>()
        val callable = callableWithDataCapture(payloadSlot)
        val source = source(
            functionsProvider = mockk {
                every { getCreateAccountAndUserDataCallable() } returns callable
            }
        )

        source.createAccount(
            email = DataFixtures.EMAIL,
            password = PASSWORD,
            name = DataFixtures.NAME,
            gender = GENDER,
            birthday = BIRTHDAY,
            address = DataFixtures.ADDRESS,
            phoneNumber = DataFixtures.PHONE_NUMBER,
            joinedAt = DataFixtures.JOINED_AT,
        )

        val payload = payloadSlot.captured as Map<*, *>
        assertEquals(DataFixtures.EMAIL, payload[AccountRemoteDataSource.KEY_EMAIL])
        assertEquals(PASSWORD, payload[AccountRemoteDataSource.KEY_PASSWORD])
        assertEquals(DataFixtures.NAME, payload[AccountRemoteDataSource.KEY_NAME])
        assertEquals(GENDER, payload[AccountRemoteDataSource.KEY_GENDER])
        assertEquals(BIRTHDAY, payload[AccountRemoteDataSource.KEY_BIRTHDAY])
        assertEquals(DataFixtures.ADDRESS, payload[AccountRemoteDataSource.KEY_ADDRESS])
        assertEquals(DataFixtures.PHONE_NUMBER, payload[AccountRemoteDataSource.KEY_PHONE_NUMBER])
        assertEquals(DataFixtures.JOINED_AT, payload[AccountRemoteDataSource.KEY_JOINED_AT])
    }

    @Test
    fun `createAccount rejects blank required fields before callable request`() = runDataTest {
        val functionsProvider = mockk<FirebaseFunctionsProvider>()
        val source = source(functionsProvider = functionsProvider)

        assertThrows(IllegalArgumentException::class.java) {
            runDataTest {
                source.createAccount(
                    email = "",
                    password = PASSWORD,
                    name = DataFixtures.NAME,
                    gender = GENDER,
                    birthday = BIRTHDAY,
                    address = DataFixtures.ADDRESS,
                    phoneNumber = DataFixtures.PHONE_NUMBER,
                    joinedAt = DataFixtures.JOINED_AT,
                )
            }
        }
        verify(exactly = 0) { functionsProvider.getCreateAccountAndUserDataCallable() }
    }

    @Test
    fun `signIn maps auth result user to account model`() = runDataTest {
        val authResult = mockk<AuthResult> {
            every { user } returns firebaseUser()
        }
        val authProvider = mockk<FirebaseAuthProvider> {
            every { signInWithEmailAndPassword(DataFixtures.EMAIL, PASSWORD) } returns Tasks.forResult(authResult)
        }
        val source = source(authProvider = authProvider)

        assertEquals(
            AccountModel(uid = DataFixtures.UID, email = DataFixtures.EMAIL),
            source.signIn(DataFixtures.EMAIL, PASSWORD),
        )
    }

    @Test
    fun `signIn throws user not found when auth result has no user`() = runDataTest {
        val authResult = mockk<AuthResult> {
            every { user } returns null
        }
        val authProvider = mockk<FirebaseAuthProvider> {
            every { signInWithEmailAndPassword(DataFixtures.EMAIL, PASSWORD) } returns Tasks.forResult(authResult)
        }
        val source = source(authProvider = authProvider)

        try {
            source.signIn(DataFixtures.EMAIL, PASSWORD)
            fail("Expected UserNotFoundException")
        } catch (_: AccountException.UserNotFoundException) {
            // Expected.
        }
    }

    @Test
    fun `checkEmailAvailability sends email payload and returns boolean response`() = runDataTest {
        val payloadSlot = slot<Any>()
        val callable = callableWithDataCapture(
            payloadSlot = payloadSlot,
            resultData = mapOf("isAvailable" to true),
        )
        val source = source(
            functionsProvider = mockk {
                every { getCheckEmailAvailabilityCallable() } returns callable
            }
        )

        assertTrue(source.checkEmailAvailability(DataFixtures.EMAIL))

        val payload = payloadSlot.captured as Map<*, *>
        assertEquals(DataFixtures.EMAIL, payload[AccountRemoteDataSource.KEY_EMAIL])
    }

    @Test
    fun `checkEmailAvailability returns false when callable response is malformed`() = runDataTest {
        val callable = callableWithDataCapture(
            payloadSlot = slot(),
            resultData = mapOf("unexpected" to true),
        )
        val source = source(
            functionsProvider = mockk {
                every { getCheckEmailAvailabilityCallable() } returns callable
            }
        )

        assertFalse(source.checkEmailAvailability(DataFixtures.EMAIL))
    }

    @Test
    fun `deleteAccount reloads reauthenticates calls delete function signs out and returns uid`() = runDataTest {
        withMockedCredential { credential ->
            val user = firebaseUser()
            every { user.reload() } returns Tasks.forResult(null)
            every { user.reauthenticate(credential) } returns Tasks.forResult(null)
            val deleteCallable = mockk<HttpsCallableReference> {
                every { call() } returns Tasks.forResult(mockk<HttpsCallableResult>())
            }
            val authProvider = mockk<FirebaseAuthProvider> {
                every { currentUser } returnsMany listOf(user, user)
                every { signOut() } returns Unit
            }
            val source = source(
                authProvider = authProvider,
                functionsProvider = mockk {
                    every { getDeleteAccountAndUserDataCallable() } returns deleteCallable
                },
            )

            assertEquals(DataFixtures.UID, source.deleteAccount(PASSWORD))

            verify(exactly = 1) { user.reload() }
            verify(exactly = 1) { user.reauthenticate(credential) }
            verify(exactly = 1) { deleteCallable.call() }
            verify(exactly = 1) { authProvider.signOut() }
        }
    }

    @Test
    fun `deleteAccount throws user not found when current user is missing`() = runDataTest {
        val functionsProvider = mockk<FirebaseFunctionsProvider>()
        val source = source(
            authProvider = mockk {
                every { currentUser } returns null
            },
            functionsProvider = functionsProvider,
        )

        try {
            source.deleteAccount(PASSWORD)
            fail("Expected UserNotFoundException")
        } catch (_: AccountException.UserNotFoundException) {
            // Expected.
        }
        verify(exactly = 0) { functionsProvider.getDeleteAccountAndUserDataCallable() }
    }

    @Test
    fun `deleteAccount throws session expired when reload reports invalid user`() = runDataTest {
        val user = firebaseUser()
        every { user.reload() } returns Tasks.forException(mockk<FirebaseAuthInvalidUserException>())
        val functionsProvider = mockk<FirebaseFunctionsProvider>()
        val source = source(
            authProvider = mockk {
                every { currentUser } returns user
            },
            functionsProvider = functionsProvider,
        )

        try {
            source.deleteAccount(PASSWORD)
            fail("Expected SessionExpiredException")
        } catch (_: AccountException.SessionExpiredException) {
            // Expected.
        }
        verify(exactly = 0) { functionsProvider.getDeleteAccountAndUserDataCallable() }
    }

    @Test
    fun `signOut delegates auth provider`() {
        val authProvider = mockk<FirebaseAuthProvider> {
            every { signOut() } returns Unit
        }
        val source = source(authProvider = authProvider)

        source.signOut()

        verify(exactly = 1) { authProvider.signOut() }
    }

    private fun source(
        authProvider: FirebaseAuthProvider = mockk(),
        functionsProvider: FirebaseFunctionsProvider = mockk(),
    ) = AccountRemoteDataSource(
        firebaseAuthProvider = authProvider,
        firebaseFunctionsProvider = functionsProvider,
    )

    private fun callableWithDataCapture(
        payloadSlot: io.mockk.CapturingSlot<Any>,
        resultData: Any? = emptyMap<String, Any>(),
    ): HttpsCallableReference {
        val result = callableResult(resultData)
        return mockk {
            every { call(capture(payloadSlot)) } returns Tasks.forResult(result)
        }
    }

    private fun callableResult(data: Any?): HttpsCallableResult {
        val constructor = HttpsCallableResult::class.java.getDeclaredConstructor(Any::class.java)
        constructor.isAccessible = true
        return constructor.newInstance(data)
    }

    private suspend fun withMockedCredential(block: suspend (AuthCredential) -> Unit) {
        val credential = mockk<AuthCredential>()
        mockkStatic(EmailAuthProvider::class)
        every { EmailAuthProvider.getCredential(DataFixtures.EMAIL, PASSWORD) } returns credential
        try {
            block(credential)
        } finally {
            unmockkStatic(EmailAuthProvider::class)
        }
    }

    private fun firebaseUser(
        uid: String = DataFixtures.UID,
        email: String? = DataFixtures.EMAIL,
    ): FirebaseUser =
        mockk {
            every { this@mockk.uid } returns uid
            every { this@mockk.email } returns email
        }

    private companion object {
        const val PASSWORD = "password-test"
        const val GENDER = "FEMALE"
        const val BIRTHDAY = "2000-01-02"
    }
}
