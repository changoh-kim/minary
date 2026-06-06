package kr.co.domain.feature.diary.usecase.setting

import kotlinx.coroutines.flow.Flow
import kr.co.domain.feature.setting.repository.UserSettingsRepository
import javax.inject.Inject

class GetDiarySettingsStreamUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
) {
    operator fun invoke(): Flow<Boolean> =
        userSettingsRepository.getDiarySyncEnabledStream()
}