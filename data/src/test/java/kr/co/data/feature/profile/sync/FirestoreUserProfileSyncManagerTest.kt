package kr.co.data.feature.profile.sync

import com.google.firebase.firestore.DocumentSnapshot
import io.mockk.every
import io.mockk.mockk
import kr.co.core.firebase.provider.FirebaseFirestoreProvider
import kr.co.core.firebase.provider.FirebaseStorageProvider
import kr.co.data.feature.profile.model.UserProfileDto
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import kr.co.data.testing.assertOk
import kr.co.data.testing.fake.FakeUserProfileLocalDataSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FirestoreUserProfileSyncManagerTest : BaseDataUnitTest() {
    @Test
    fun `pullProfile updates local profile when remote is newer`() = runDataTest {
        val local = FakeUserProfileLocalDataSource().apply {
            profileState.value = DataFixtures.userProfileProto.toBuilder()
                .setLastModifiedAt(100L)
                .build()
        }
        val manager = manager(local)

        manager.pullProfile(
            userId = DataFixtures.UID,
            snapshot = snapshot(
                DataFixtures.userProfileDto.copy(
                    lastModifiedAt = 300L,
                    profilePhotoUrl = "",
                )
            ),
        ).assertOk(Unit)

        assertEquals(300L, local.updatedProfiles.single().lastModifiedAt)
        assertEquals(emptyList<Any>(), local.downloadRequests)
    }

    @Test
    fun `pullProfile downloads profile photo when newer remote profile has photo url`() = runDataTest {
        val local = FakeUserProfileLocalDataSource().apply {
            profileState.value = DataFixtures.userProfileProto.toBuilder()
                .setLastModifiedAt(100L)
                .build()
        }
        val manager = manager(local)

        manager.pullProfile(
            userId = DataFixtures.UID,
            snapshot = snapshot(DataFixtures.userProfileDto.copy(lastModifiedAt = 300L)),
        ).assertOk(Unit)

        assertEquals(DataFixtures.UID, local.downloadRequests.single().uid)
        assertEquals(DataFixtures.PROFILE_PHOTO_URL, local.downloadRequests.single().downloadUrl)
        assertEquals(300L, local.downloadRequests.single().lastModifiedAt)
    }

    @Test
    fun `pullProfile skips update when local profile is newer`() = runDataTest {
        val local = FakeUserProfileLocalDataSource().apply {
            profileState.value = DataFixtures.userProfileProto.toBuilder()
                .setLastModifiedAt(500L)
                .build()
        }
        val manager = manager(local)

        manager.pullProfile(
            userId = DataFixtures.UID,
            snapshot = snapshot(DataFixtures.userProfileDto.copy(lastModifiedAt = 300L)),
        ).assertOk(Unit)

        assertEquals(emptyList<Any>(), local.updatedProfiles)
        assertEquals(emptyList<Any>(), local.downloadRequests)
    }

    private fun manager(local: FakeUserProfileLocalDataSource) =
        FirestoreUserProfileSyncManager(
            firebaseFirestoreProvider = mockk<FirebaseFirestoreProvider>(),
            firebaseStorageProvider = mockk<FirebaseStorageProvider>(),
            userProfileLocalDataSource = local.mock,
        )

    private fun snapshot(profile: UserProfileDto): DocumentSnapshot =
        mockk {
            every { exists() } returns true
            every { toObject(UserProfileDto::class.java) } returns profile
        }
}
