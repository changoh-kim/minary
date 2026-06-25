package kr.co.domain.feature.user.usecase

import com.github.michaelbull.result.Result
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.user.repository.UserStorageRepository
import javax.inject.Inject

class DeleteUserStorageUseCase @Inject constructor(
    private val userStorageRepository: UserStorageRepository,
) {
    suspend operator fun invoke(currentUid: String): Result<Unit, DomainError> {
        return userStorageRepository.deleteUserStorage(currentUid)
    }
}