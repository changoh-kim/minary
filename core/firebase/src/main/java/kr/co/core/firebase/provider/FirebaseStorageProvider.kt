package kr.co.core.firebase.provider

import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageProvider @Inject constructor(
    private val storage: FirebaseStorage,
) {
    companion object {
        const val DIR_PROFILE_PHOTOS = "profile_photos"
    }

    fun getUserProfilePhotoRef(uid: String) =
        storage.reference.child("${DIR_PROFILE_PHOTOS}/$uid.jpg")
}