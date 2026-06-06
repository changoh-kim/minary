package kr.co.domain.common.state

import kr.co.domain.error.DomainError

sealed class SyncProcessState {
    object Idle : SyncProcessState()
    sealed class InProgress : SyncProcessState() {
        // 진행률 표시 클래스(0.0 ~ 1.0)
        data class Determinate(val progress: Float) : InProgress()
        // 단순히 "진행 중"임을 나타내는 클래스
        object Indeterminate : InProgress()
    }

    object Completed : SyncProcessState()
    data class Failed(val error: DomainError) : SyncProcessState()
}