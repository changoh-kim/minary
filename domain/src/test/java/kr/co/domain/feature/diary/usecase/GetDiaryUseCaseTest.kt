package kr.co.domain.feature.diary.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kr.co.core.common.error.DomainError
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import kr.co.domain.testing.fake.FakeDiaryRepository
import org.junit.jupiter.api.Test

class GetDiaryUseCaseTest : DomainCoroutineTest() {

    @Test
    fun `returns diary from repository`() {
        runDomainTest {
            val diary = DomainFixtures.diary()
            val repository = FakeDiaryRepository().apply {
                getDiaryResult = Ok(diary)
            }
            val useCase = GetDiaryUseCase(repository)

            val result = useCase(DomainFixtures.DATE)

            result.assertOk(diary)
        }
    }

    @Test
    fun `returns repository failure`() {
        runDomainTest {
            val error = DomainError.Store.NotFound
            val repository = FakeDiaryRepository().apply {
                getDiaryResult = Err(error)
            }
            val useCase = GetDiaryUseCase(repository)

            val result = useCase(DomainFixtures.DATE)

            result.assertErr(error)
        }
    }
}
