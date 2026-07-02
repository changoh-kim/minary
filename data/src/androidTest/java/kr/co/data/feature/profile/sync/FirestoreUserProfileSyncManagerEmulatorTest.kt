package kr.co.data.feature.profile.sync

import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await
import kr.co.core.datastore.proto.GenderProto
import kr.co.core.datastore.proto.UserProfileProto
import kr.co.core.firebase.provider.FirebaseFirestoreProvider
import kr.co.core.firebase.provider.FirebaseStorageProvider
import kr.co.data.feature.profile.model.UserProfileDto
import kr.co.data.testing.AndroidDownloadProfilePhotoRequest
import kr.co.data.testing.AndroidFakeUserProfileLocalDataSource
import kr.co.data.testing.BaseFirebaseEmulatorTest
import kr.co.data.testing.assertAndroidOk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreUserProfileSyncManagerEmulatorTest : BaseFirebaseEmulatorTest() {

    @Test
    fun syncProfile_creates_remote_document_when_missing() = runDataAndroidTest {
        val user = createSignedInEmulatorUser()
        val firestoreProvider = FirebaseFirestoreProvider(firebaseFirestore)
        val local = AndroidFakeUserProfileLocalDataSource(
            profile(
                uid = user.uid,
                name = "local-name-test",
                nickname = "local-nickname-test",
                profilePhotoUrl = "",
                lastModifiedAt = 300L,
            )
        )
        val manager = manager(firestoreProvider, local)

        manager.syncProfile(user.uid).assertAndroidOk(Unit)

        val snapshot = profileSnapshot(firestoreProvider, user.uid)
        assertTrue(snapshot.exists())
        assertEquals(user.uid, snapshot.getString(UserProfileDto.UID))
        assertEquals("local-name-test", snapshot.getString(UserProfileDto.NAME))
        assertEquals("local-nickname-test", snapshot.getString(UserProfileDto.NICKNAME))
        assertEquals(300L, snapshot.getLong(UserProfileDto.LAST_MODIFIED_AT))
        assertEquals(emptyList<UserProfileProto>(), local.updatedProfiles)
        assertEquals(emptyList<AndroidDownloadProfilePhotoRequest>(), local.downloadRequests)
    }

    @Test
    fun syncProfile_pulls_remote_document_and_downloads_photo_when_remote_is_newer() =
        runDataAndroidTest {
            val user = createSignedInEmulatorUser()
            val firestoreProvider = FirebaseFirestoreProvider(firebaseFirestore)
            val local = AndroidFakeUserProfileLocalDataSource(
                profile(
                    uid = user.uid,
                    name = "local-name-test",
                    nickname = "local-nickname-test",
                    profilePhotoUrl = "",
                    lastModifiedAt = 100L,
                )
            )
            val manager = manager(firestoreProvider, local)
            seedRemoteProfile(
                firestoreProvider = firestoreProvider,
                uid = user.uid,
                profile = profileDto(
                    uid = user.uid,
                    name = "remote-name-test",
                    nickname = "remote-nickname-test",
                    profilePhotoUrl = REMOTE_PROFILE_PHOTO_URL,
                    lastModifiedAt = 300L,
                )
            )

            manager.syncProfile(user.uid).assertAndroidOk(Unit)

            val pulledProfile = local.updatedProfiles.single()
            assertEquals("remote-name-test", pulledProfile.name)
            assertEquals("remote-nickname-test", pulledProfile.nickname)
            assertEquals(300L, pulledProfile.lastModifiedAt)
            assertEquals(
                AndroidDownloadProfilePhotoRequest(
                    uid = user.uid,
                    downloadUrl = REMOTE_PROFILE_PHOTO_URL,
                    lastModifiedAt = 300L,
                ),
                local.downloadRequests.single(),
            )
        }

    @Test
    fun syncProfile_pushes_local_fields_without_overwriting_remote_photo_when_local_is_newer() =
        runDataAndroidTest {
            val user = createSignedInEmulatorUser()
            val firestoreProvider = FirebaseFirestoreProvider(firebaseFirestore)
            val local = AndroidFakeUserProfileLocalDataSource(
                profile(
                    uid = user.uid,
                    name = "local-name-test",
                    nickname = "local-nickname-test",
                    profilePhotoUrl = "file:///tmp/local-profile-photo-test.jpg",
                    lastModifiedAt = 500L,
                )
            )
            val manager = manager(firestoreProvider, local)
            seedRemoteProfile(
                firestoreProvider = firestoreProvider,
                uid = user.uid,
                profile = profileDto(
                    uid = user.uid,
                    name = "remote-name-test",
                    nickname = "remote-nickname-test",
                    profilePhotoUrl = REMOTE_PROFILE_PHOTO_URL,
                    lastModifiedAt = 100L,
                )
            )

            manager.syncProfile(user.uid).assertAndroidOk(Unit)

            val snapshot = profileSnapshot(firestoreProvider, user.uid)
            assertEquals("local-name-test", snapshot.getString(UserProfileDto.NAME))
            assertEquals("local-nickname-test", snapshot.getString(UserProfileDto.NICKNAME))
            assertEquals(500L, snapshot.getLong(UserProfileDto.LAST_MODIFIED_AT))
            assertEquals(REMOTE_PROFILE_PHOTO_URL, snapshot.getString(UserProfileDto.PROFILE_PHOTO_URL))
            assertEquals(emptyList<UserProfileProto>(), local.updatedProfiles)
            assertEquals(emptyList<AndroidDownloadProfilePhotoRequest>(), local.downloadRequests)
        }

    private fun manager(
        firestoreProvider: FirebaseFirestoreProvider,
        local: AndroidFakeUserProfileLocalDataSource,
    ) = FirestoreUserProfileSyncManager(
        firebaseFirestoreProvider = firestoreProvider,
        firebaseStorageProvider = FirebaseStorageProvider(firebaseStorage),
        userProfileLocalDataSource = local.mock,
    )

    private suspend fun seedRemoteProfile(
        firestoreProvider: FirebaseFirestoreProvider,
        uid: String,
        profile: UserProfileDto,
    ) {
        firestoreProvider
            .getUserProfileRef(uid)
            .set(profile)
            .await()
    }

    private suspend fun profileSnapshot(
        firestoreProvider: FirebaseFirestoreProvider,
        uid: String,
    ): DocumentSnapshot =
        firestoreProvider
            .getUserProfileRef(uid)
            .get()
            .await()

    private fun profile(
        uid: String,
        name: String,
        nickname: String,
        profilePhotoUrl: String,
        lastModifiedAt: Long,
    ): UserProfileProto =
        UserProfileProto.newBuilder()
            .setUid(uid)
            .setEmail("email-value-test")
            .setName(name)
            .setGender(GenderProto.FEMALE)
            .setBirthday("2000-01-02")
            .setAddress("address-test")
            .setPhoneNumber("phone-number-test")
            .setNickname(nickname)
            .setProfilePhotoUrl(profilePhotoUrl)
            .setJoinedAt(100L)
            .setLastModifiedAt(lastModifiedAt)
            .build()

    private fun profileDto(
        uid: String,
        name: String,
        nickname: String,
        profilePhotoUrl: String,
        lastModifiedAt: Long,
    ): UserProfileDto =
        UserProfileDto(
            uid = uid,
            email = "email-value-test",
            name = name,
            gender = GenderProto.FEMALE.name,
            birthday = "2000-01-02",
            address = "address-test",
            phoneNumber = "phone-number-test",
            nickname = nickname,
            profilePhotoUrl = profilePhotoUrl,
            joinedAt = 100L,
            lastModifiedAt = lastModifiedAt,
        )

    private companion object {
        const val REMOTE_PROFILE_PHOTO_URL = "https://example.test/profile-photo-test.jpg"
    }
}
