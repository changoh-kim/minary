package kr.co.domain.feature.diary.usecase.sync

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import kr.co.domain.error.DomainError
import kr.co.domain.feature.diary.repository.DiaryRepository
import kr.co.domain.feature.session.repository.SessionRepository
import java.time.YearMonth
import javax.inject.Inject

class RequestMonthSyncUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(yearMonth: YearMonth): Result<Unit, DomainError> = coroutineBinding {
        val userSession = sessionRepository.getCurrentUser().bind()
        diaryRepository.requestMonthSync(userSession.uid, yearMonth)
    }
}