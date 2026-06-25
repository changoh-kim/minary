package kr.co.data.feature.profile.sync

import androidx.core.net.toUri
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.storageMetadata
import kotlinx.coroutines.tasks.await
import kr.co.data.extension.toDomainError
import kr.co.data.feature.profile.mapper.UserProfileMapper.toUserProfileDto
import kr.co.data.feature.profile.mapper.UserProfileMapper.toUserProfileProto
import kr.co.data.feature.profile.model.UserProfileDto
import kr.co.data.feature.profile.source.local.UserProfileLocalDataSource
import kr.co.core.firebase.provider.FirebaseFirestoreProvider
import kr.co.core.firebase.provider.FirebaseStorageProvider
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.profile.sync.UserProfileSyncManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserProfileSyncManager @Inject constructor(
    private val firebaseFirestoreProvider: FirebaseFirestoreProvider,
    private val firebaseStorageProvider: FirebaseStorageProvider,
    private val userProfileLocalDataSource: UserProfileLocalDataSource,
) : UserProfileSyncManager {

    /**
     * 사용자 프로필을 서버와 Sync
     *
     * @param userId
     */
    override suspend fun syncProfile(userId: String): Result<Unit, DomainError> {
        return runSuspendCatching {
            val localProfile = userProfileLocalDataSource.getUserProfile()

            val ref = firebaseFirestoreProvider.getUserProfileRef(userId)
            val snapshot = ref.get().await()

            if (!snapshot.exists()) {
                ref.set(localProfile.toUserProfileDto(), SetOptions.merge()).await()
            } else {
                val remoteProfile = snapshot.toObject(UserProfileDto::class.java)
                when {
                    // 서버에 데이터가 없거나 로컬이 최신이면 (로컬에서 서버로 Push)
                    (remoteProfile == null) || (remoteProfile.lastModifiedAt < localProfile.lastModifiedAt) -> {
                        // Firestore의 사용자 프로필 업데이트 (이미지는 별도 syncProfilePhoto에서 관리하므로 제외)
                        val localProfileDto = localProfile.toUserProfileDto()
                        val updateData = mapOf(
                            UserProfileDto.UID to localProfileDto.uid,
                            UserProfileDto.EMAIL to localProfileDto.email,
                            UserProfileDto.NAME to localProfileDto.name,
                            UserProfileDto.GENDER to localProfileDto.gender,
                            UserProfileDto.BIRTHDAY to localProfileDto.birthday,
                            UserProfileDto.ADDRESS to localProfileDto.address,
                            UserProfileDto.PHONE_NUMBER to localProfileDto.phoneNumber,
                            UserProfileDto.NICKNAME to localProfileDto.nickname,
                            UserProfileDto.JOINED_AT to localProfileDto.joinedAt,
                            UserProfileDto.LAST_MODIFIED_AT to localProfileDto.lastModifiedAt,
                        )
                        ref.set(updateData, SetOptions.merge()).await()
                    }

                    // 서버가 최신(서버에서 로컬로 Pull)
                    remoteProfile.lastModifiedAt > localProfile.lastModifiedAt -> {
                        userProfileLocalDataSource.updateUserProfile(remoteProfile.toUserProfileProto())

                        // 서버 사진 다운로드 및 로컬 경로 동기화
                        if (remoteProfile.profilePhotoUrl.isNotEmpty()) {
                            userProfileLocalDataSource.downloadAndSyncProfilePhoto(
                                userId,
                                remoteProfile.profilePhotoUrl,
                                remoteProfile.lastModifiedAt
                            )
                        }
                    }
                }
            }
        }.map { Unit }
        .mapError { it.toDomainError() }
    }

    /**
     * 사진을 제외한 사용자 프로필을 서버로 Push
     *
     * @param userId
     */
    override suspend fun pushProfile(userId: String): Result<Unit, DomainError> {
        return runSuspendCatching {
            val localProfile = userProfileLocalDataSource.getUserProfile()

            val ref = firebaseFirestoreProvider.getUserProfileRef(userId)
            val snapshot = ref.get().await()

            if (!snapshot.exists()) {
                ref.set(localProfile.toUserProfileDto(), SetOptions.merge()).await()
            } else {
                val remoteProfile = snapshot.toObject(UserProfileDto::class.java)
                if (remoteProfile != null && remoteProfile.lastModifiedAt < localProfile.lastModifiedAt) {
                    // Firestore의 사용자 프로필 업데이트 (이미지는 별도 syncProfilePhoto에서 관리하므로 제외)
                    val localProfileDto = localProfile.toUserProfileDto()
                    val updateData = mapOf(
                        UserProfileDto.UID to localProfileDto.uid,
                        UserProfileDto.EMAIL to localProfileDto.email,
                        UserProfileDto.NAME to localProfileDto.name,
                        UserProfileDto.GENDER to localProfileDto.gender,
                        UserProfileDto.BIRTHDAY to localProfileDto.birthday,
                        UserProfileDto.ADDRESS to localProfileDto.address,
                        UserProfileDto.PHONE_NUMBER to localProfileDto.phoneNumber,
                        UserProfileDto.NICKNAME to localProfileDto.nickname,
                        UserProfileDto.JOINED_AT to localProfileDto.joinedAt,
                        UserProfileDto.LAST_MODIFIED_AT to localProfileDto.lastModifiedAt,
                    )
                    ref.set(updateData, SetOptions.merge()).await()
                }
            }
        }.map { Unit }
        .mapError { it.toDomainError() }
    }

    /**
     * 사용자 프로필을 Pull
     *
     * @param userId
     */
    override suspend fun pullProfile(userId: String): Result<Unit, DomainError> {
        val result = runSuspendCatching {
            val ref = firebaseFirestoreProvider.getUserProfileRef(userId)
            ref.get().await()
        }.mapError { it.toDomainError() }

        return result.andThen { snapshot ->
            pullProfile(userId, snapshot)
        }
    }

    /**
     * 실제 사용자 프로필 Pull을 수행하는 함수
     *
     * @param userId
     * @param snapshot
     */
    internal suspend fun pullProfile(userId: String, snapshot: DocumentSnapshot): Result<Unit, DomainError> {
        return runSuspendCatching {
            if (snapshot.exists()) {
                val remoteProfile = snapshot.toObject(UserProfileDto::class.java)
                remoteProfile?.let {
                    val localProfile = userProfileLocalDataSource.getUserProfile()
                    if (remoteProfile.lastModifiedAt > localProfile.lastModifiedAt) {
                        userProfileLocalDataSource.updateUserProfile(remoteProfile.toUserProfileProto())

                        // 서버 사진 다운로드 및 로컬 경로 동기화
                        if (remoteProfile.profilePhotoUrl.isNotEmpty()) {
                            userProfileLocalDataSource.downloadAndSyncProfilePhoto(
                                userId,
                                remoteProfile.profilePhotoUrl,
                                remoteProfile.lastModifiedAt
                            )
                        }
                    }
                }
            }
        }.map { Unit }
        .mapError { it.toDomainError() }
    }

    /**
     * 사용자 프로필 사진을 서버로 Push
     *
     * @param userId
     */
    override suspend fun pushProfilePhoto(userId: String): Result<Unit, DomainError> {
        return runSuspendCatching {
            val localProfile = userProfileLocalDataSource.getUserProfile()

            val storageRef = firebaseStorageProvider.getUserProfilePhotoRef(userId)
            val remoteMetadata = try {
                storageRef.metadata.await()
            } catch (e: Exception) {
                null
            }

            /* [사용자 프로필 사진]의 LWW 정책을 지키기 위해서는 로컬 사용자 프로필의 lastModifiedAt과
            firestore의 사용자 프로필의 lastModifiedAt과 비교하지 않고,
            firebase storage 서버에 저장된 사진 파일 메타데이터의 lastModifiedAt을 비교합니다.
            상세 이유는 -- storage 업데이트 주석 -- 참고 */
            val remoteLastModifiedAt =
                remoteMetadata?.getCustomMetadata(UserProfileDto.LAST_MODIFIED_AT)?.toLongOrNull()
                    ?: remoteMetadata?.updatedTimeMillis
                    ?: 0L

            if (localProfile.lastModifiedAt > remoteLastModifiedAt) {
                if (localProfile.profilePhotoUrl.startsWith("file://") || localProfile.profilePhotoUrl.startsWith("/")) {
                    val uri = localProfile.profilePhotoUrl.toUri()
                    val metadata = storageMetadata {
                        setCustomMetadata(
                            UserProfileDto.LAST_MODIFIED_AT,
                            localProfile.lastModifiedAt.toString()
                        )
                    }
                    /* -- storage 업데이트 주석 --
                    사진 파일에 메타데이터로 수정시간을 담아 firebase storage에 업데이트하면
                    index.ts 파일에 정의한 onProfilePhotoUploaded 이벤트 함수가 트리거 됩니다.
                    이벤트 함수는 firestore 사용자 프로필 정보에 downloadUrl, lastModifiedAt을 업데이트하여
                    firestore 사용자 프로필의 원자성을 갖게하며 개발자는 [사용자 프로필]만 참조하면 됩니다.
                    다만 이번처럼 [사용자 프로필 사진] 업데이트의 경우에 메타데이터의 lastModifiedAt만 참조하더라도
                    문제가 발생하지 않기 때문에 서버 [사용자 프로필] 데이터의 lastModifiedAt를 참조하지 않았습니다.*/
                    storageRef.putFile(uri, metadata).await()
                }
            }
        }.map { Unit }
        .mapError { it.toDomainError() }
    }
}