package kr.co.domain.feature.account.usecase

import com.github.michaelbull.result.Result
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.account.service.AccountService
import kr.co.domain.feature.diary.sync.DiarySyncScheduler
import kr.co.domain.feature.diary.usecase.sync.StopRealtimeDiarySyncUseCase
import kr.co.domain.feature.profile.usecase.sync.StopRealtimeUserProfileSyncUseCase
import kr.co.domain.feature.setting.usecase.sync.StopRealtimeUserSettingsSyncUseCase
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val diarySyncScheduler: DiarySyncScheduler,
    private val stopRealtimeDiarySync: StopRealtimeDiarySyncUseCase,
    private val stopRealtimeUserProfileSync: StopRealtimeUserProfileSyncUseCase,
    private val stopRealtimeUserSettingsSync: StopRealtimeUserSettingsSyncUseCase,
    private val accountService: AccountService,
) {
    suspend operator fun invoke(): Result<Unit, DomainError> {
        // Immediate 동기화를 제외한 나머지 취소
        diarySyncScheduler.cancelFullSync()
        diarySyncScheduler.cancelPeriodicSync()

        stopRealtimeDiarySync()
        stopRealtimeUserProfileSync()
        stopRealtimeUserSettingsSync()

        return accountService.signOut()
    }
}