package kr.co.domain.feature.user.usecase

import kr.co.core.common.result.AppResult
import kr.co.domain.feature.user.repository.UserStorageRepository
import javax.inject.Inject

class DeleteUserStorageUseCase @Inject constructor(
    private val userStorageRepository: UserStorageRepository,
) {
    suspend operator fun invoke(currentUid: String): AppResult<Unit> {
        return userStorageRepository.deleteUserStorage(currentUid)
    }
}