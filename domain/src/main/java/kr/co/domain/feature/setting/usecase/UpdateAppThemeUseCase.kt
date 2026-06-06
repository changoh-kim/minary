package kr.co.domain.feature.setting.usecase

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import kr.co.domain.error.DomainError
import kr.co.domain.feature.session.repository.SessionRepository
import kr.co.domain.feature.setting.model.AppTheme
import kr.co.domain.feature.setting.repository.UserSettingsRepository
import kr.co.domain.feature.time.service.ServerTimeProvider
import javax.inject.Inject

class UpdateAppThemeUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val serverTimeProvider: ServerTimeProvider,
    private val settingsRepository: UserSettingsRepository,
) {
    suspend operator fun invoke(appTheme: AppTheme): Result<Unit, DomainError> = coroutineBinding {
        val user = sessionRepository.getCurrentUser().bind()
        val lastModifiedAt = serverTimeProvider.now()
        settingsRepository.updateAppTheme(user.uid, appTheme, lastModifiedAt).bind()
    }
}