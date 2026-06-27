package kr.co.domain.feature.user.usecase

import com.github.michaelbull.result.coroutines.coroutineBinding
import kr.co.core.common.result.AppResult
import kr.co.domain.feature.session.repository.SessionRepository
import javax.inject.Inject

class InitUserStorageUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val deleteUserStorage: DeleteUserStorageUseCase,
) {
    suspend operator fun invoke(currentUid: String): AppResult<Unit> = coroutineBinding {
        val lastUid = sessionRepository.getLastSignInUid()

        if (lastUid != null && lastUid != currentUid) {
            deleteUserStorage(lastUid).bind()
        }

        sessionRepository.setLastSignInUid(currentUid)
    }
}
