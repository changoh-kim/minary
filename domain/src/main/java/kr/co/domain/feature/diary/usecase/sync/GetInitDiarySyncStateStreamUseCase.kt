package kr.co.domain.feature.diary.usecase.sync

import kotlinx.coroutines.flow.StateFlow
import kr.co.domain.common.state.SyncProcessState
import kr.co.domain.feature.diary.repository.sync.DiarySyncStateRepository
import javax.inject.Inject

class GetInitDiarySyncStateStreamUseCase @Inject constructor(
    private val syncStateRepository: DiarySyncStateRepository,
) {
    operator fun invoke(): StateFlow<SyncProcessState> {
        return syncStateRepository.initDiarySyncState
    }
}