package kr.co.domain.feature.setting.usecase

import kr.co.core.common.result.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kr.co.domain.feature.setting.model.UserSettings
import kr.co.domain.feature.setting.repository.UserSettingsRepository
import javax.inject.Inject


class GetUserSettingsStreamUseCase @Inject constructor(
    private val settingsRepository: UserSettingsRepository,
) {
    suspend operator fun invoke(): Flow<AppResult<UserSettings>> {
        return settingsRepository.getUserSettingsStream().distinctUntilChanged()
    }
}