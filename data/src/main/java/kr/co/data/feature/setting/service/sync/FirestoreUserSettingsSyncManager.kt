package kr.co.data.feature.setting.service.sync

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kr.co.data.extension.toDomainError
import kr.co.data.feature.setting.mapper.UserSettingsMapper.toUserSettingsDto
import kr.co.data.feature.setting.mapper.UserSettingsMapper.toUserSettingsProto
import kr.co.data.feature.setting.model.UserSettingsDto
import kr.co.data.feature.setting.source.local.UserSettingsLocalDataSource
import kr.co.data.remote.firebase.provider.FirebaseFirestoreProvider
import kr.co.domain.error.DomainError
import kr.co.domain.feature.setting.service.sync.UserSettingsSyncManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserSettingsSyncManager @Inject constructor(
    private val firebaseFirestoreProvider: FirebaseFirestoreProvider,
    private val localDataSource: UserSettingsLocalDataSource,
) : UserSettingsSyncManager {

    override suspend fun syncSettings(userId: String): Result<Unit, DomainError> {
        return runSuspendCatching {
            val localSettings = localDataSource.getUserSettings()

            val ref = firebaseFirestoreProvider.getUserSettingsRef(userId)
            val snapshot = ref.get().await()

            if (!snapshot.exists()) {
                ref.set(localSettings.toUserSettingsDto(), SetOptions.merge()).await()
            } else {
                val remoteSettings = snapshot.toObject(UserSettingsDto::class.java)
                remoteSettings?.let {
                    when {
                        // 로컬이 최신이면 Push
                        it.lastModifiedAt < localSettings.lastModifiedAt -> {
                            ref.set(localSettings.toUserSettingsDto(), SetOptions.merge()).await()
                        }
                        // 서버가 최신이면 Pull
                        it.lastModifiedAt > localSettings.lastModifiedAt -> {
                            localDataSource.updateUserSettings(it.toUserSettingsProto())
                        }
                    }
                }
            }
        }.map { Unit }
        .mapError { it.toDomainError() }
    }

    override suspend fun pushSettings(userId: String): Result<Unit, DomainError> {
        return runSuspendCatching {
            val localSettings = localDataSource.getUserSettings()

            val ref = firebaseFirestoreProvider.getUserSettingsRef(userId)
            val snapshot = ref.get().await()

            if (!snapshot.exists()) {
                ref.set(localSettings.toUserSettingsDto(), SetOptions.merge()).await()
            } else {
                val remoteSettings = snapshot.toObject(UserSettingsDto::class.java)
                if (remoteSettings != null && remoteSettings.lastModifiedAt < localSettings.lastModifiedAt) {
                    ref.set(localSettings.toUserSettingsDto(), SetOptions.merge()).await()
                }
            }
        }.map { Unit }
        .mapError { it.toDomainError() }
    }

    override suspend fun pullSettings(userId: String): Result<Unit, DomainError> {
        val result = runSuspendCatching {
            val ref = firebaseFirestoreProvider.getUserSettingsRef(userId)
            ref.get().await()
        }.mapError { it.toDomainError() }

        return result.andThen { snapshot ->
            pullSettings(snapshot)
        }
    }

    // 실제 Pull 로직 처리 함수
    internal suspend fun pullSettings(snapshot: DocumentSnapshot): Result<Unit, DomainError> {
        return runSuspendCatching {
            if (snapshot.exists()) {
                val remoteSettings = snapshot.toObject(UserSettingsDto::class.java)
                remoteSettings?.let {
                    val localSettings = localDataSource.getUserSettings()
                    if (it.lastModifiedAt > localSettings.lastModifiedAt) {
                        localDataSource.updateUserSettings(it.toUserSettingsProto())
                    }
                }
            }
        }.map { Unit }.mapError { it.toDomainError() }
    }
}