package kr.co.core.ui.common.load

import androidx.annotation.CheckResult
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.fold
import com.github.michaelbull.result.map
import kotlinx.coroutines.CancellationException
import kr.co.core.common.error.DomainError
import kr.co.core.common.result.AppResult

/**
 * `AppResult`를 반환하는 비동기 작업을 UI 상태 처리와 함께 실행합니다.
 */
@CheckResult(suggest = "startOnSuccess, startAsLoadState 또는 start를 호출하여 작업을 완료해야 합니다.")
fun <T> load(
    action: suspend () -> AppResult<T>
): SafeLoad<T, T> {
    return SafeLoad(action) { it }
}

class SafeLoad<T, R>(
    private val action: suspend () -> AppResult<T>,
    private val mapper: suspend (T) -> R
) {
    private var onStartBlock: (suspend () -> Unit)? = null
    private var onFinallyBlock: (suspend () -> Unit)? = null
    private var onLoadingBlock: (suspend (Boolean) -> Unit)? = null
    private var onErrorBlock: (suspend (DomainError) -> Unit)? = null

    @CheckResult
    fun <NEW_R> map(newMapper: suspend (R) -> NEW_R): SafeLoad<T, NEW_R> {
        return SafeLoad(action) { result ->
            newMapper(mapper(result))
        }.also { newBuilder ->
            newBuilder.onStartBlock = this.onStartBlock
            newBuilder.onFinallyBlock = this.onFinallyBlock
            newBuilder.onLoadingBlock = this.onLoadingBlock
            newBuilder.onErrorBlock = this.onErrorBlock
        }
    }

    @CheckResult
    fun onStart(block: suspend () -> Unit) = apply {
        val oldBlock = this.onStartBlock
        this.onStartBlock = 
            if (oldBlock == null) block 
            else { { oldBlock(); block() } }
    }

    @CheckResult
    fun onFinally(block: suspend () -> Unit) = apply {
        val oldBlock = this.onFinallyBlock
        this.onFinallyBlock = 
            if (oldBlock == null) block 
            else { { oldBlock(); block() } }
    }

    @CheckResult
    fun onLoading(block: suspend (isLoading: Boolean) -> Unit) = apply {
        val oldBlock = this.onLoadingBlock
        this.onLoadingBlock =
            if (oldBlock == null) block 
            else { isLoading -> oldBlock(isLoading); block(isLoading) }
    }

    @CheckResult
    fun onError(block: suspend (error: DomainError) -> Unit) = apply {
        val oldBlock = this.onErrorBlock
        this.onErrorBlock =
            if (oldBlock == null) block 
            else { error -> oldBlock(error); block(error) }
    }

    suspend fun startAsLoadState(
        onLoadStateBlock: suspend (loadState: LoadState<R>) -> Unit
    ) {
        onStartBlock?.invoke()
        onLoadingBlock?.invoke(true)
        onLoadStateBlock(LoadState.Loading)

        executeAction().fold(
            success = { data -> onLoadStateBlock(LoadState.Success(data)) },
            failure = { error ->
                onLoadStateBlock(LoadState.Error(error))
                onErrorBlock?.invoke(error)
            }
        )

        onLoadingBlock?.invoke(false)
        onFinallyBlock?.invoke()
    }

    suspend fun startOnSuccess(
        onSuccessBlock: (suspend (R) -> Unit)? = null
    ) {
        onStartBlock?.invoke()
        onLoadingBlock?.invoke(true)

        executeAction().fold(
            success = { data -> onSuccessBlock?.invoke(data) },
            failure = { error -> onErrorBlock?.invoke(error) }
        )

        onLoadingBlock?.invoke(false)
        onFinallyBlock?.invoke()
    }

    suspend fun start() {
        onStartBlock?.invoke()
        onLoadingBlock?.invoke(true)

        executeAction().fold(
            success = { _ -> },
            failure = { error -> onErrorBlock?.invoke(error) }
        )

        onLoadingBlock?.invoke(false)
        onFinallyBlock?.invoke()
    }

    private suspend fun executeAction(): AppResult<R> {
        return try {
            action().map { mapper(it) }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Err(DomainError.Unexpected)
        }
    }
}
