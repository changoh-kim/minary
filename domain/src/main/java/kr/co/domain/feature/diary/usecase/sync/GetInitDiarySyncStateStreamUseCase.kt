package kr.co.domain.feature.diary.usecase.sync

import kotlinx.coroutines.flow.StateFlow
import kr.co.core.common.state.SyncProcessState
import kr.co.domain.feature.diary.sync.DiarySyncStateRepository
import javax.inject.Inject

class GetInitDiarySyncStateStreamUseCase @Inject constructor(
    private val syncStateRepository: DiarySyncStateRepository,
) {
    operator fun invoke(): StateFlow<SyncProcessState> {
        return syncStateRepository.initDiarySyncState
    }
}