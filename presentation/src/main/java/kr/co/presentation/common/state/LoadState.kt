package kr.co.presentation.common.state

import androidx.compose.runtime.Immutable
import kr.co.domain.error.DomainError

@Immutable
sealed interface LoadState<out T> {
    data object Uninitialized : LoadState<Nothing>
    data object Loading : LoadState<Nothing>
    data class Success<T>(val data: T) : LoadState<T>
    data class Error(
        val error: DomainError? = null,
        val message: String? = null
    ) : LoadState<Nothing> {
        constructor(throwable: Throwable?, message: String? = null) : this(
            error = throwable?.let { DomainError.Unexpected(it) },
            message = message ?: throwable?.message
        )
    }

    val isUninitialized: Boolean get() = this is Uninitialized
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
}

/**
 * UiState가 Success 상태일 경우 데이터를 반환하고, 그렇지 않으면 null 반환합니다.
 */
val <T> LoadState<T>.data: T?
    get() = (this as? LoadState.Success)?.data