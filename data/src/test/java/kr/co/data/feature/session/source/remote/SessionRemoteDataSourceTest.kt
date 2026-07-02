package kr.co.data.feature.session.source.remote

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runCurrent
import kr.co.core.firebase.provider.FirebaseAuthProvider
import kr.co.data.feature.account.exception.AccountException
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionRemoteDataSourceTest : BaseDataUnitTest() {
    @Test
    fun `getCurrentUser maps firebase user to session model`() = runDataTest {
        val source = SessionRemoteDataSource(
            firebaseAuthProvider = authProviderWithCurrentUser(firebaseUser()),
        )

        assertEquals(DataFixtures.userSessionModel, source.getCurrentUser())
    }

    @Test
    fun `getCurrentUser throws user not found when current user is missing`() = runDataTest {
        val source = SessionRemoteDataSource(
            firebaseAuthProvider = authProviderWithoutCurrentUser(),
        )

        assertThrows(AccountException.UserNotFoundException::class.java) {
            source.getCurrentUser()
        }
    }

    @Test
    fun `reload refreshes firebase user and returns latest current user`() = runDataTest {
        val firebaseUser = firebaseUser(uid = "stale-uid-test")
        val refreshedUser = firebaseUser(uid = DataFixtures.UID)
        every { firebaseUser.reload() } returns Tasks.forResult(null)
        val authProvider = mockk<FirebaseAuthProvider> {
            every { currentUser } returnsMany listOf(firebaseUser, refreshedUser)
        }
        val source = SessionRemoteDataSource(authProvider)

        assertEquals(DataFixtures.userSessionModel, source.reload())
    }

    @Test
    fun `reload throws session expired when firebase user becomes invalid`() = runDataTest {
        val firebaseUser = firebaseUser()
        every { firebaseUser.reload() } returns Tasks.forException(
            mockk<FirebaseAuthInvalidUserException>()
        )
        val source = SessionRemoteDataSource(
            firebaseAuthProvider = authProviderWithCurrentUser(firebaseUser),
        )

        try {
            source.reload()
            fail("Expected SessionExpiredException")
        } catch (_: AccountException.SessionExpiredException) {
            // Expected.
        }
    }

    @Test
    fun `observeSessionStateFlow emits auth changes and removes listener on close`() = runDataTest {
        val listenerSlot = slot<FirebaseAuth.AuthStateListener>()
        val authProvider = mockk<FirebaseAuthProvider> {
            every { addAuthStateListener(capture(listenerSlot)) } just Runs
            every { removeAuthStateListener(any()) } just Runs
        }
        val source = SessionRemoteDataSource(authProvider)
        val values = async {
            source.observeSessionStateFlow()
                .take(2)
                .toList()
        }
        runCurrent()

        listenerSlot.captured.onAuthStateChanged(firebaseAuth(firebaseUser()))
        listenerSlot.captured.onAuthStateChanged(firebaseAuth(null))

        assertEquals(listOf(DataFixtures.userSessionModel, null), values.await())
        verify(exactly = 1) { authProvider.removeAuthStateListener(listenerSlot.captured) }
    }

    private fun authProviderWithCurrentUser(firebaseUser: FirebaseUser): FirebaseAuthProvider =
        mockk {
            every { currentUser } returns firebaseUser
        }

    private fun authProviderWithoutCurrentUser(): FirebaseAuthProvider =
        mockk {
            every { currentUser } returns null
        }

    private fun firebaseUser(
        uid: String = DataFixtures.UID,
        email: String? = DataFixtures.EMAIL,
    ): FirebaseUser =
        mockk {
            every { this@mockk.uid } returns uid
            every { this@mockk.email } returns email
        }

    private fun firebaseAuth(firebaseUser: FirebaseUser?): FirebaseAuth =
        mockk {
            every { currentUser } returns firebaseUser
        }
}
