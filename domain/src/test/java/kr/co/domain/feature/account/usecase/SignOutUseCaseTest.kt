package kr.co.domain.feature.account.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.account.service.AccountService
import kr.co.domain.feature.diary.sync.DiarySyncScheduler
import kr.co.domain.feature.diary.usecase.sync.StopRealtimeDiarySyncUseCase
import kr.co.domain.feature.profile.usecase.sync.StopRealtimeUserProfileSyncUseCase
import kr.co.domain.feature.setting.usecase.sync.StopRealtimeUserSettingsSyncUseCase
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import org.junit.jupiter.api.Test

class SignOutUseCaseTest : DomainCoroutineTest() {

    private val diarySyncScheduler = mockk<DiarySyncScheduler>(relaxed = true)
    private val stopRealtimeDiarySync = mockk<StopRealtimeDiarySyncUseCase>()
    private val stopRealtimeUserProfileSync = mockk<StopRealtimeUserProfileSyncUseCase>()
    private val stopRealtimeUserSettingsSync = mockk<StopRealtimeUserSettingsSyncUseCase>()
    private val accountService = mockk<AccountService>()
    private val useCase = SignOutUseCase(
        diarySyncScheduler = diarySyncScheduler,
        stopRealtimeDiarySync = stopRealtimeDiarySync,
        stopRealtimeUserProfileSync = stopRealtimeUserProfileSync,
        stopRealtimeUserSettingsSync = stopRealtimeUserSettingsSync,
        accountService = accountService,
    )

    @Test
    fun `cancels non immediate sync and realtime sync before signing out`() {
        runDomainTest {
            every { stopRealtimeDiarySync() } returns Unit
            every { stopRealtimeUserProfileSync() } returns Unit
            every { stopRealtimeUserSettingsSync() } returns Unit
            coEvery { accountService.signOut() } returns Ok(Unit)

            val result = useCase()

            result.assertOk(Unit)
            verify(exactly = 1) { diarySyncScheduler.cancelFullSync() }
            verify(exactly = 1) { diarySyncScheduler.cancelPeriodicSync() }
            verify(exactly = 0) { diarySyncScheduler.cancelImmediateSync() }
            verify(exactly = 1) { stopRealtimeDiarySync() }
            verify(exactly = 1) { stopRealtimeUserProfileSync() }
            verify(exactly = 1) { stopRealtimeUserSettingsSync() }
            coVerify(exactly = 1) { accountService.signOut() }
        }
    }

    @Test
    fun `returns sign out failure after stopping sync work`() {
        runDomainTest {
            val error = DomainError.NetworkUnavailable
            every { stopRealtimeDiarySync() } returns Unit
            every { stopRealtimeUserProfileSync() } returns Unit
            every { stopRealtimeUserSettingsSync() } returns Unit
            coEvery { accountService.signOut() } returns Err(error)

            val result = useCase()

            result.assertErr(error)
            verify(exactly = 1) { diarySyncScheduler.cancelFullSync() }
            verify(exactly = 1) { diarySyncScheduler.cancelPeriodicSync() }
            verify(exactly = 1) { stopRealtimeDiarySync() }
            verify(exactly = 1) { stopRealtimeUserProfileSync() }
            verify(exactly = 1) { stopRealtimeUserSettingsSync() }
            coVerify(exactly = 1) { accountService.signOut() }
        }
    }
}
