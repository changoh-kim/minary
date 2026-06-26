package kr.co.domain.feature.diary.usecase.sync

import kr.co.core.common.result.AppResult
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kr.co.core.common.state.SyncProcessState
import kr.co.domain.feature.diary.sync.DiarySyncStateRepository
import kr.co.domain.feature.diary.sync.DiarySyncManager
import kr.co.domain.feature.session.repository.SessionRepository
import javax.inject.Inject

class InitialDiarySyncUseCase @Inject constructor(
    private val syncStateRepository: DiarySyncStateRepository,
    private val sessionRepository: SessionRepository,
    private val diarySyncManager: DiarySyncManager
) {
    suspend operator fun invoke(): AppResult<Unit> =
        coroutineBinding {
            syncStateRepository.updateInitDiarySyncState(
                SyncProcessState.InProgress.Determinate(0f)
            )

            val user = sessionRepository.getCurrentUser().bind()

            diarySyncManager.performInitialPull(user.uid) { progress ->
                // Repository에서 진행률 콜백으로 업데이트
                syncStateRepository.updateInitDiarySyncState(
                    SyncProcessState.InProgress.Determinate(progress)
                )
            }.bind()

            syncStateRepository.setInitialSyncCompleted(true)
        }.onOk {
            syncStateRepository.updateInitDiarySyncState(SyncProcessState.Completed)
        }.onErr {
            syncStateRepository.updateInitDiarySyncState(SyncProcessState.Failed(it))
        }
}