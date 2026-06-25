package kr.co.domain.feature.setting.usecase

import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.setting.model.UserSettings
import kr.co.domain.feature.setting.repository.UserSettingsRepository
import javax.inject.Inject


class GetUserSettingsStreamUseCase @Inject constructor(
    private val settingsRepository: UserSettingsRepository,
) {
    suspend operator fun invoke(): Flow<Result<UserSettings, DomainError>> {
        return settingsRepository.getUserSettingsStream().distinctUntilChanged()
    }
}