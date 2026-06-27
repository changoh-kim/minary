package kr.co.domain.feature.diary.usecase.sync

import kr.co.core.common.state.SyncProcessState
import kr.co.domain.testing.fake.FakeDiarySyncStateRepository
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class GetInitDiarySyncStateStreamUseCaseTest {

    @Test
    fun `returns init diary sync state flow`() {
        val repository = FakeDiarySyncStateRepository(initialState = SyncProcessState.Completed)
        val useCase = GetInitDiarySyncStateStreamUseCase(repository)

        val result = useCase()

        assertSame(repository.initDiarySyncState, result)
    }
}
