package kr.co.domain.feature.diary.usecase.setting

import kr.co.core.common.result.AppResult
import com.github.michaelbull.result.coroutines.coroutineBinding
import kr.co.domain.feature.session.repository.SessionRepository
import kr.co.domain.feature.setting.repository.UserSettingsRepository
import kr.co.domain.service.time.ServerTimeProvider
import javax.inject.Inject

class UpdateDiarySyncEnabledUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val serverTimeProvider: ServerTimeProvider,
    private val settingsRepository: UserSettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean): AppResult<Unit> = coroutineBinding {
        val user = sessionRepository.getCurrentUser().bind()
        val lastModifiedAt = serverTimeProvider.now()
        settingsRepository.updateDiarySyncEnabled(user.uid, enabled, lastModifiedAt).bind()
    }
}