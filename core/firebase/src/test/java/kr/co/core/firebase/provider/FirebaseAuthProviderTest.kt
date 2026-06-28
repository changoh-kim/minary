package kr.co.core.firebase.provider

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class FirebaseAuthProviderTest {

    private val auth = mockk<FirebaseAuth>()
    private val provider = FirebaseAuthProvider(auth)

    @Test
    fun `delegates current user lookup`() {
        val user = mockk<FirebaseUser>()
        every { auth.currentUser } returns user

        assertSame(user, provider.currentUser)
    }

    @Test
    fun `delegates sign in and sign out calls`() {
        val task = mockk<Task<AuthResult>>()
        every { auth.signInWithEmailAndPassword("email-test", "value-test") } returns task
        every { auth.signOut() } just runs

        assertSame(task, provider.signInWithEmailAndPassword("email-test", "value-test"))
        provider.signOut()

        verify(exactly = 1) { auth.signInWithEmailAndPassword("email-test", "value-test") }
        verify(exactly = 1) { auth.signOut() }
    }

    @Test
    fun `delegates auth state listener registration`() {
        val listener = mockk<FirebaseAuth.AuthStateListener>()
        every { auth.addAuthStateListener(listener) } just runs
        every { auth.removeAuthStateListener(listener) } just runs

        provider.addAuthStateListener(listener)
        provider.removeAuthStateListener(listener)

        verify(exactly = 1) { auth.addAuthStateListener(listener) }
        verify(exactly = 1) { auth.removeAuthStateListener(listener) }
    }
}
