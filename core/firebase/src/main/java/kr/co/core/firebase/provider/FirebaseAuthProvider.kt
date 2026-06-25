package kr.co.core.firebase.provider

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthProvider @Inject constructor(
    private val auth: FirebaseAuth,
) {
    val currentUser get() = auth.currentUser

    fun signInWithEmailAndPassword(email: String, password: String) =
        auth.signInWithEmailAndPassword(email, password)

    fun signOut() = auth.signOut()

    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.addAuthStateListener(listener)
    }

    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.removeAuthStateListener(listener)
    }
}
