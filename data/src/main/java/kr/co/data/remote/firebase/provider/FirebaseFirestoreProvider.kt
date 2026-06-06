package kr.co.data.remote.firebase.provider

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction.Function
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreProvider @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    companion object {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_PROFILE = "profile"
        const val COLLECTION_SETTINGS = "settings"
        const val COLLECTION_DIARIES = "diaries"

        const val DOCUMENT_USER_PROFILE = "userProfile"
        const val DOCUMENT_USER_SETTINGS = "userSettings"
    }

    fun getDiariesRef(uid: String) =
        firestore
            .collection(COLLECTION_USERS)
            .document(uid)
            .collection(COLLECTION_DIARIES)

    fun getUserProfileRef(uid: String) =
        firestore
            .collection(COLLECTION_USERS)
            .document(uid)
            .collection(COLLECTION_PROFILE)
            .document(DOCUMENT_USER_PROFILE)

    fun getUserSettingsRef(uid: String) =
        firestore
            .collection(COLLECTION_USERS)
            .document(uid)
            .collection(COLLECTION_SETTINGS)
            .document(DOCUMENT_USER_SETTINGS)

    fun batch() = firestore.batch()

    fun <T> runTransaction(updateFunction: Function<T>) =
        firestore.runTransaction(updateFunction)
}