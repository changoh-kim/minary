package kr.co.domain.feature.account.usecase

import kr.co.core.common.result.AppResult
import com.github.michaelbull.result.coroutines.coroutineBinding
import kr.co.domain.feature.account.service.AccountService
import kr.co.domain.feature.diary.usecase.sync.StopAllDiarySyncUseCase
import kr.co.domain.feature.profile.usecase.sync.StopAllUserProfileSyncUseCase
import kr.co.domain.feature.setting.usecase.sync.StopAllUserSettingsSyncUseCase
import kr.co.domain.feature.user.usecase.DeleteUserStorageUseCase
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val accountService: AccountService,
    private val stopAllDiarySync: StopAllDiarySyncUseCase,
    private val stopAllUserProfileSync: StopAllUserProfileSyncUseCase,
    private val stopAllUserSettingsSync: StopAllUserSettingsSyncUseCase,
    private val deleteUserStorage: DeleteUserStorageUseCase,
) {
    suspend operator fun invoke(password: String): AppResult<Unit> = coroutineBinding {
        val uid = accountService.deleteAccount(password).bind()

        stopAllDiarySync()
        stopAllUserProfileSync()
        stopAllUserSettingsSync()

        deleteUserStorage(uid).bind()
    }
}
