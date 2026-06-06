package kr.co.domain.feature.user.usecase

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kr.co.domain.error.DomainError
import kr.co.domain.feature.session.repository.SessionRepository
import javax.inject.Inject

class InitUserStorageUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val deleteUserStorage: DeleteUserStorageUseCase,
) {
    suspend operator fun invoke(currentUid: String): Result<Unit, DomainError> {
        val lastUid = sessionRepository.getLastSignInUid()

        if (lastUid != null && lastUid != currentUid) {
            deleteUserStorage(lastUid)
        }

        sessionRepository.setLastSignInUid(currentUid)
        return Ok(Unit)
    }
}