package kr.co.domain.feature.diary.usecase.sync

import kr.co.core.common.state.SyncStatus
import kr.co.domain.testing.fake.FakeDiaryRepository
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.time.YearMonth

class GetMonthSyncStatusStreamUseCaseTest {

    @Test
    fun `returns sync status stream for requested month`() {
        val repository = FakeDiaryRepository()
        val useCase = GetMonthSyncStatusStreamUseCase(repository)
        val target = YearMonth.of(2026, 6)
        repository.setSyncStatus(target, SyncStatus.SYNCED)
        val expected = repository.getSyncStatusStream(target)

        val result = useCase(target)

        assertSame(expected, result)
    }
}
