package kr.co.data.feature.profile.repository

import kr.co.core.common.logging.AppLogger
import android.net.Uri
import com.github.michaelbull.result.Ok
import kr.co.core.common.result.AppResult
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onErr
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kr.co.data.extension.toDomainError
import kr.co.data.feature.profile.mapper.UserProfileMapper.toUserProfile
import kr.co.data.feature.profile.mapper.UserProfileMapper.toUserProfileProto
import kr.co.data.feature.profile.source.local.UserProfileLocalDataSource
import kr.co.domain.feature.profile.model.UserProfile
import kr.co.domain.feature.profile.repository.UserProfileRepository
import kr.co.domain.service.image.ImageProcessor
import kr.co.domain.feature.profile.sync.UserProfileSyncScheduler
import java.io.File
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val logger: AppLogger,
    private val localDataSource: UserProfileLocalDataSource,
    private val profileScheduler: UserProfileSyncScheduler,
    private val imageProcessor: ImageProcessor,
) : UserProfileRepository {

    override suspend fun getUserProfileStream(): Flow<AppResult<UserProfile>> =
        localDataSource.getUserProfileFlow().map { Ok(it.toUserProfile()) }

    override suspend fun updateUserProfile(
        profile: UserProfile
    ): AppResult<Unit> =
        runSuspendCatching {
            localDataSource.updateUserProfile(profile.toUserProfileProto())
            profileScheduler.scheduleProfilePush()
        }
        .onErr { logger.e(it, "Failed to save user profile") }
        .mapError { it.toDomainError() }

    override suspend fun updateUserProfilePhoto(
        uid: String,
        photoUrl: String,
        lastModifiedAt: Long,
    ): AppResult<String> {
        val targetUrl = localDataSource.getProfilePhotoFilePath(uid)

        // 1. 이미지 리사이즈
        return imageProcessor.resizeImage(
            sourceUrl = photoUrl,
            targetUrl = targetUrl
        ).andThen { resizedPath ->
            runSuspendCatching {
                val newProfilePhotoUrl = Uri.fromFile(File(resizedPath)).toString()

                // 2. 로컬 업데이트 및 서버 업데이트 스케줄링 예약
                localDataSource.updateUserProfilePhotoUrl(newProfilePhotoUrl, lastModifiedAt)
                profileScheduler.scheduleProfilePhotoPush()

                newProfilePhotoUrl
            }.onErr { logger.e(it, "Failed to upload profile photo") }
            .mapError { it.toDomainError() }
        }
    }
}