package kr.co.data.feature.setting.sync

import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await
import kr.co.core.datastore.proto.ThemeProto
import kr.co.core.datastore.proto.UserSettingsProto
import kr.co.core.firebase.provider.FirebaseFirestoreProvider
import kr.co.data.feature.setting.model.UserSettingsDto
import kr.co.data.testing.AndroidFakeUserSettingsLocalDataSource
import kr.co.data.testing.BaseFirebaseEmulatorTest
import kr.co.data.testing.assertAndroidOk
import org.junit.Assert.assertEquals
import org.junit.Test

class FirestoreUserSettingsSyncManagerEmulatorTest : BaseFirebaseEmulatorTest() {

    @Test
    fun syncSettings_creates_remote_document_when_missing() = runDataAndroidTest {
        val user = createSignedInEmulatorUser()
        val firestoreProvider = FirebaseFirestoreProvider(firebaseFirestore)
        val local = AndroidFakeUserSettingsLocalDataSource(
            settings(
                theme = ThemeProto.DARK,
                diarySyncEnabled = true,
                lastModifiedAt = 300L,
            )
        )
        val manager = manager(firestoreProvider, local)

        manager.syncSettings(user.uid).assertAndroidOk(Unit)

        val snapshot = settingsSnapshot(firestoreProvider, user.uid)
        assertEquals(true, snapshot.exists())
        assertEquals(ThemeProto.DARK.name, snapshot.getString(UserSettingsDto.THEME))
        assertEquals(true, snapshot.getBoolean(UserSettingsDto.DIARY_SYNC_ENABLED))
        assertEquals(300L, snapshot.getLong(UserSettingsDto.LAST_MODIFIED_AT))
        assertEquals(emptyList<UserSettingsProto>(), local.updatedSettings)
    }

    @Test
    fun syncSettings_pulls_remote_document_when_remote_is_newer() = runDataAndroidTest {
        val user = createSignedInEmulatorUser()
        val firestoreProvider = FirebaseFirestoreProvider(firebaseFirestore)
        val local = AndroidFakeUserSettingsLocalDataSource(
            settings(
                theme = ThemeProto.SYSTEM,
                diarySyncEnabled = false,
                lastModifiedAt = 100L,
            )
        )
        val manager = manager(firestoreProvider, local)
        seedRemoteSettings(
            firestoreProvider = firestoreProvider,
            uid = user.uid,
            theme = ThemeProto.LIGHT,
            diarySyncEnabled = true,
            lastModifiedAt = 300L,
        )

        manager.syncSettings(user.uid).assertAndroidOk(Unit)

        val pulledSettings = local.updatedSettings.single()
        assertEquals(ThemeProto.LIGHT, pulledSettings.theme)
        assertEquals(true, pulledSettings.diarySyncEnabled)
        assertEquals(300L, pulledSettings.lastModifiedAt)
    }

    @Test
    fun syncSettings_pushes_local_document_when_local_is_newer() = runDataAndroidTest {
        val user = createSignedInEmulatorUser()
        val firestoreProvider = FirebaseFirestoreProvider(firebaseFirestore)
        val local = AndroidFakeUserSettingsLocalDataSource(
            settings(
                theme = ThemeProto.DARK,
                diarySyncEnabled = true,
                lastModifiedAt = 500L,
            )
        )
        val manager = manager(firestoreProvider, local)
        seedRemoteSettings(
            firestoreProvider = firestoreProvider,
            uid = user.uid,
            theme = ThemeProto.SYSTEM,
            diarySyncEnabled = false,
            lastModifiedAt = 100L,
        )

        manager.syncSettings(user.uid).assertAndroidOk(Unit)

        val snapshot = settingsSnapshot(firestoreProvider, user.uid)
        assertEquals(ThemeProto.DARK.name, snapshot.getString(UserSettingsDto.THEME))
        assertEquals(true, snapshot.getBoolean(UserSettingsDto.DIARY_SYNC_ENABLED))
        assertEquals(500L, snapshot.getLong(UserSettingsDto.LAST_MODIFIED_AT))
        assertEquals(emptyList<UserSettingsProto>(), local.updatedSettings)
    }

    private fun manager(
        firestoreProvider: FirebaseFirestoreProvider,
        local: AndroidFakeUserSettingsLocalDataSource,
    ) = FirestoreUserSettingsSyncManager(
        firebaseFirestoreProvider = firestoreProvider,
        localDataSource = local.mock,
    )

    private suspend fun seedRemoteSettings(
        firestoreProvider: FirebaseFirestoreProvider,
        uid: String,
        theme: ThemeProto,
        diarySyncEnabled: Boolean,
        lastModifiedAt: Long,
    ) {
        firestoreProvider
            .getUserSettingsRef(uid)
            .set(settingsMap(theme, diarySyncEnabled, lastModifiedAt))
            .await()
    }

    private suspend fun settingsSnapshot(
        firestoreProvider: FirebaseFirestoreProvider,
        uid: String,
    ): DocumentSnapshot =
        firestoreProvider
            .getUserSettingsRef(uid)
            .get()
            .await()

    private fun settings(
        theme: ThemeProto,
        diarySyncEnabled: Boolean,
        lastModifiedAt: Long,
    ): UserSettingsProto =
        UserSettingsProto.newBuilder()
            .setTheme(theme)
            .setDiarySyncEnabled(diarySyncEnabled)
            .setLastModifiedAt(lastModifiedAt)
            .build()

    private fun settingsMap(
        theme: ThemeProto,
        diarySyncEnabled: Boolean,
        lastModifiedAt: Long,
    ): Map<String, Any> =
        mapOf(
            UserSettingsDto.THEME to theme.name,
            UserSettingsDto.DIARY_SYNC_ENABLED to diarySyncEnabled,
            UserSettingsDto.LAST_MODIFIED_AT to lastModifiedAt,
        )
}
