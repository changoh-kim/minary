package kr.co.domain.feature.setting.usecase

import kr.co.core.common.result.AppResult
import com.github.michaelbull.result.coroutines.coroutineBinding
import kr.co.domain.feature.session.repository.SessionRepository
import kr.co.core.common.model.AppTheme
import kr.co.domain.feature.setting.repository.UserSettingsRepository
import kr.co.domain.service.time.ServerTimeProvider
import javax.inject.Inject

class UpdateAppThemeUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val serverTimeProvider: ServerTimeProvider,
    private val settingsRepository: UserSettingsRepository,
) {
    suspend operator fun invoke(appTheme: AppTheme): AppResult<Unit> = coroutineBinding {
        val user = sessionRepository.getCurrentUser().bind()
        val lastModifiedAt = serverTimeProvider.now()
        settingsRepository.updateAppTheme(user.uid, appTheme, lastModifiedAt).bind()
    }
}