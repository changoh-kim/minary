package kr.co.domain.feature.setting.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kr.co.core.common.model.AppTheme
import kr.co.domain.feature.setting.repository.UserSettingsRepository
import javax.inject.Inject

class GetAppThemeStreamUseCase @Inject constructor(
    private val settingsRepository: UserSettingsRepository,
) {
    operator fun invoke(): Flow<AppTheme> {
        return settingsRepository.getAppThemeStream().distinctUntilChanged()
    }
}