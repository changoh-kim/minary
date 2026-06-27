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
import kr.co.domain.feature.diary.usecase.sync.StopAllDiarySyncUseCase
import kr.co.domain.feature.profile.usecase.sync.StopAllUserProfileSyncUseCase
import kr.co.domain.feature.setting.usecase.sync.StopAllUserSettingsSyncUseCase
import kr.co.domain.feature.user.usecase.DeleteUserStorageUseCase
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import kr.co.domain.testing.fake.FakeUserStorageRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DeleteAccountUseCaseTest : DomainCoroutineTest() {

    private val accountService = mockk<AccountService>()
    private val stopAllDiarySync = mockk<StopAllDiarySyncUseCase>()
    private val stopAllUserProfileSync = mockk<StopAllUserProfileSyncUseCase>()
    private val stopAllUserSettingsSync = mockk<StopAllUserSettingsSyncUseCase>()
    private val userStorageRepository = FakeUserStorageRepository()
    private val deleteUserStorage = DeleteUserStorageUseCase(userStorageRepository)
    private val useCase = DeleteAccountUseCase(
        accountService = accountService,
        stopAllDiarySync = stopAllDiarySync,
        stopAllUserProfileSync = stopAllUserProfileSync,
        stopAllUserSettingsSync = stopAllUserSettingsSync,
        deleteUserStorage = deleteUserStorage,
    )

    @Test
    fun `does not clean up sync or storage when account deletion fails`() {
        runDomainTest {
            val error = DomainError.Auth.RequiresRecentLogin
            coEvery { accountService.deleteAccount("value-test") } returns Err(error)

            val result = useCase("value-test")

            result.assertErr(error)
            coVerify(exactly = 1) { accountService.deleteAccount("value-test") }
            verify(exactly = 0) { stopAllDiarySync() }
            verify(exactly = 0) { stopAllUserProfileSync() }
            verify(exactly = 0) { stopAllUserSettingsSync() }
            assertEquals(emptyList<String>(), userStorageRepository.deletedUids)
        }
    }

    @Test
    fun `stops all sync work and deletes returned user storage when account deletion succeeds`() {
        runDomainTest {
            coEvery { accountService.deleteAccount("value-test") } returns Ok(DomainFixtures.UID)
            every { stopAllDiarySync() } returns Unit
            every { stopAllUserProfileSync() } returns Unit
            every { stopAllUserSettingsSync() } returns Unit

            val result = useCase("value-test")

            result.assertOk(Unit)
            verify(exactly = 1) { stopAllDiarySync() }
            verify(exactly = 1) { stopAllUserProfileSync() }
            verify(exactly = 1) { stopAllUserSettingsSync() }
            assertEquals(listOf(DomainFixtures.UID), userStorageRepository.deletedUids)
        }
    }

    @Test
    fun `returns storage deletion failure after stopping sync work`() {
        runDomainTest {
            val error = DomainError.Storage.PermissionDenied
            userStorageRepository.deleteUserStorageResult = Err(error)
            coEvery { accountService.deleteAccount("value-test") } returns Ok(DomainFixtures.UID)
            every { stopAllDiarySync() } returns Unit
            every { stopAllUserProfileSync() } returns Unit
            every { stopAllUserSettingsSync() } returns Unit

            val result = useCase("value-test")

            result.assertErr(error)
            verify(exactly = 1) { stopAllDiarySync() }
            verify(exactly = 1) { stopAllUserProfileSync() }
            verify(exactly = 1) { stopAllUserSettingsSync() }
            assertEquals(listOf(DomainFixtures.UID), userStorageRepository.deletedUids)
        }
    }
}
