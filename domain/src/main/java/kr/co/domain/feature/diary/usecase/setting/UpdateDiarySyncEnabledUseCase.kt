package kr.co.domain.feature.diary.usecase.setting

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import kr.co.domain.error.DomainError
import kr.co.domain.feature.session.repository.SessionRepository
import kr.co.domain.feature.setting.repository.UserSettingsRepository
import kr.co.domain.feature.time.service.ServerTimeProvider
import javax.inject.Inject

class UpdateDiarySyncEnabledUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val serverTimeProvider: ServerTimeProvider,
    private val settingsRepository: UserSettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit, DomainError> = coroutineBinding {
        val user = sessionRepository.getCurrentUser().bind()
        val lastModifiedAt = serverTimeProvider.now()
        settingsRepository.updateDiarySyncEnabled(user.uid, enabled, lastModifiedAt).bind()
    }
}