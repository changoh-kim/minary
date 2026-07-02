package kr.co.data.feature.setting.sync

import com.google.firebase.firestore.DocumentSnapshot
import io.mockk.every
import io.mockk.mockk
import kr.co.core.datastore.proto.ThemeProto
import kr.co.core.firebase.provider.FirebaseFirestoreProvider
import kr.co.data.feature.setting.model.UserSettingsDto
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import kr.co.data.testing.assertOk
import kr.co.data.testing.fake.FakeUserSettingsLocalDataSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FirestoreUserSettingsSyncManagerTest : BaseDataUnitTest() {
    @Test
    fun `pullSettings updates local settings when remote is newer`() = runDataTest {
        val local = FakeUserSettingsLocalDataSource().apply {
            settingsState.value = DataFixtures.userSettingsProto.toBuilder()
                .setTheme(ThemeProto.SYSTEM)
                .setDiarySyncEnabled(false)
                .setLastModifiedAt(100L)
                .build()
        }
        val manager = manager(local)

        manager.pullSettings(
            snapshot(
                DataFixtures.userSettingsDto.copy(
                    theme = ThemeProto.DARK.name,
                    diarySyncEnabled = true,
                    lastModifiedAt = 300L,
                )
            )
        ).assertOk(Unit)

        assertEquals(ThemeProto.DARK, local.updatedSettings.single().theme)
        assertEquals(true, local.updatedSettings.single().diarySyncEnabled)
        assertEquals(300L, local.updatedSettings.single().lastModifiedAt)
    }

    @Test
    fun `pullSettings skips update when local settings is newer`() = runDataTest {
        val local = FakeUserSettingsLocalDataSource().apply {
            settingsState.value = DataFixtures.userSettingsProto.toBuilder()
                .setLastModifiedAt(500L)
                .build()
        }
        val manager = manager(local)

        manager.pullSettings(
            snapshot(DataFixtures.userSettingsDto.copy(lastModifiedAt = 300L))
        ).assertOk(Unit)

        assertEquals(emptyList<Any>(), local.updatedSettings)
    }

    @Test
    fun `pullSettings ignores missing remote document`() = runDataTest {
        val local = FakeUserSettingsLocalDataSource()
        val manager = manager(local)

        manager.pullSettings(
            mockk<DocumentSnapshot> {
                every { exists() } returns false
            }
        ).assertOk(Unit)

        assertEquals(emptyList<Any>(), local.updatedSettings)
    }

    private fun manager(local: FakeUserSettingsLocalDataSource) =
        FirestoreUserSettingsSyncManager(
            firebaseFirestoreProvider = mockk<FirebaseFirestoreProvider>(),
            localDataSource = local.mock,
        )

    private fun snapshot(settings: UserSettingsDto): DocumentSnapshot =
        mockk {
            every { exists() } returns true
            every { toObject(UserSettingsDto::class.java) } returns settings
        }
}
