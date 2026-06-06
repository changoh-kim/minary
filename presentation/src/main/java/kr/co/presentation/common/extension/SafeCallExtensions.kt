package kr.co.presentation.common.extension

import androidx.annotation.CheckResult
import com.github.michaelbull.result.fold
import com.github.michaelbull.result.map
import kotlinx.coroutines.CancellationException
import kr.co.presentation.common.state.LoadState
import com.github.michaelbull.result.Result as MRResult

/**
 * 코틀린 비동기 작업 체이닝 빌더입니다.
 * `Result`를 반환하는 `action`을 받아, 체이닝 가능한 `SafeCall`을 생성합니다.
 */
@CheckResult(suggest = "launchOnSuccess, launchAsLoadState 또는 launch를 호출하여 작업을 완료해야 합니다.")
fun <T> safeCall(
    action: suspend () -> Result<T>
): SafeCall<T, T> {
    return SafeCall(action) { it }
}

/**
 * 범용 비동기 작업을 위한 빌더 클래스입니다.
 */
class SafeCall<T, R>(
    private val action: suspend () -> Result<T>,
    private val mapper: suspend (T) -> R
) {
    private var onStartBlock: (suspend () -> Unit)? = null
    private var onFinallyBlock: (suspend () -> Unit)? = null
    private var onLoadingBlock: (suspend (Boolean) -> Unit)? = null
    private var onErrorBlock: (suspend (Throwable) -> Unit)? = null

    @CheckResult
    fun <NEW_R> map(newMapper: suspend (R) -> NEW_R): SafeCall<T, NEW_R> {
        return SafeCall(action) { result ->
            newMapper(mapper(result))
        }.also { newBuilder ->
            newBuilder.onStartBlock = this.onStartBlock
            newBuilder.onFinallyBlock = this.onFinallyBlock
            newBuilder.onErrorBlock = this.onErrorBlock
            newBuilder.onLoadingBlock = this.onLoadingBlock
        }
    }

    @CheckResult
    fun onStart(block: suspend () -> Unit) = apply {
        val oldBlock = this.onStartBlock
        this.onStartBlock = if (oldBlock == null) block else {
            { oldBlock(); block() }
        }
    }

    @CheckResult
    fun onFinally(block: suspend () -> Unit) = apply {
        val oldBlock = this.onFinallyBlock
        this.onFinallyBlock = if (oldBlock == null) block else {
            { oldBlock(); block() }
        }
    }

    @CheckResult
    fun onLoading(block: suspend (isLoading: Boolean) -> Unit) = apply {
        val oldBlock = this.onLoadingBlock
        this.onLoadingBlock =
            if (oldBlock == null) block else { isLoading -> oldBlock(isLoading); block(isLoading) }
    }

    @CheckResult
    fun onError(block: suspend (error: Throwable) -> Unit) = apply {
        val oldBlock = this.onErrorBlock
        this.onErrorBlock =
            if (oldBlock == null) block else { error -> oldBlock(error); block(error) }
    }

    /**
     * 종단 연산자
     * 요청을 실행하고 결과를 LoadState 타입으로 반환합니다.
     */
    suspend fun launchAsLoadState(
        onLoadStateBlock: suspend (loadState: LoadState<R>) -> Unit
    ) {
        onStartBlock?.invoke()
        onLoadingBlock?.invoke(true)
        onLoadStateBlock(LoadState.Loading)

        executeAction().fold(
            onSuccess = { data -> onLoadStateBlock(LoadState.Success(data)) },
            onFailure = { error ->
                onLoadStateBlock(LoadState.Error(error))
                onErrorBlock?.invoke(error)
            }
        )

        onLoadingBlock?.invoke(false)
        onFinallyBlock?.invoke()
    }

    /**
     * 종단 연산자
     * 요청을 실행하고 결과가 성공적으로 처리된 경우 onSuccessBlock을 실행합니다.
     */
    suspend fun launchOnSuccess(
        onSuccessBlock: (suspend (R) -> Unit)? = null
    ) {
        onStartBlock?.invoke()
        onLoadingBlock?.invoke(true)

        executeAction().fold(
            onSuccess = { data -> onSuccessBlock?.invoke(data) },
            onFailure = { error -> onErrorBlock?.invoke(error) }
        )

        onLoadingBlock?.invoke(false)
        onFinallyBlock?.invoke()
    }

    /**
     * 종단 연산자
     * 요청을 실행하지만 결과를 반환하지 않습니다.
     */
    suspend fun launch() {
        onStartBlock?.invoke()
        onLoadingBlock?.invoke(true)

        executeAction().fold(
            onSuccess = { _ -> },
            onFailure = { error -> onErrorBlock?.invoke(error) }
        )

        onLoadingBlock?.invoke(false)
        onFinallyBlock?.invoke()
    }

    private suspend fun executeAction(): Result<R> {
        return try {
            action().mapCatching { mapper(it) }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}

class MRResultError(val error: Any) : Exception("Result.Err: $error")

/**
 * kotlin-result의 `Result<V, E>`를 안전하게 실행하기 위한 오버로딩 함수입니다.
 */
@CheckResult(suggest = "launchOnSuccess, launchAsLoadState 또는 launch를 호출하여 작업을 완료해야 합니다.")
fun <V, E : Any> safeCall(
    action: suspend () -> MRResult<V, E>
): SafeCallMR<V, V, E> {
    return SafeCallMR(action) { it }
}

class SafeCallMR<T, R, E : Any>(
    private val action: suspend () -> MRResult<T, E>,
    private val mapper: suspend (T) -> R
) {
    private var onStartBlock: (suspend () -> Unit)? = null
    private var onFinallyBlock: (suspend () -> Unit)? = null
    private var onLoadingBlock: (suspend (Boolean) -> Unit)? = null
    private var onErrorBlock: (suspend (E) -> Unit)? = null // typed error

    @CheckResult
    fun <NEW_R> map(newMapper: suspend (R) -> NEW_R): SafeCallMR<T, NEW_R, E> {
        return SafeCallMR(action) { result ->
            newMapper(mapper(result))
        }.also { newBuilder ->
            newBuilder.onStartBlock = this.onStartBlock
            newBuilder.onFinallyBlock = this.onFinallyBlock
            newBuilder.onErrorBlock = this.onErrorBlock
            newBuilder.onLoadingBlock = this.onLoadingBlock
        }
    }

    @CheckResult
    fun onStart(block: suspend () -> Unit) = apply {
        val oldBlock = this.onStartBlock
        this.onStartBlock = if (oldBlock == null) block else {
            { oldBlock(); block() }
        }
    }

    @CheckResult
    fun onFinally(block: suspend () -> Unit) = apply {
        val oldBlock = this.onFinallyBlock
        this.onFinallyBlock = if (oldBlock == null) block else {
            { oldBlock(); block() }
        }
    }

    @CheckResult
    fun onLoading(block: suspend (isLoading: Boolean) -> Unit) = apply {
        val oldBlock = this.onLoadingBlock
        this.onLoadingBlock =
            if (oldBlock == null) block else { isLoading -> oldBlock(isLoading); block(isLoading) }
    }

    @CheckResult
    fun onError(block: suspend (error: E) -> Unit) = apply {
        val oldBlock = this.onErrorBlock
        this.onErrorBlock =
            if (oldBlock == null) block else { error -> oldBlock(error); block(error) }
    }

    suspend fun launchAsLoadState(
        onLoadStateBlock: suspend (loadState: LoadState<R>) -> Unit
    ) {
        onStartBlock?.invoke()
        onLoadingBlock?.invoke(true)
        onLoadStateBlock(LoadState.Loading)

        executeAction().fold(
            success = { data -> onLoadStateBlock(LoadState.Success(data)) },
            failure = { error ->
                onLoadStateBlock(LoadState.Error(MRResultError(error)))
                onErrorBlock?.invoke(error)
            }
        )

        onLoadingBlock?.invoke(false)
        onFinallyBlock?.invoke()
    }

    suspend fun launchOnSuccess(
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

    suspend fun launch() {
        onStartBlock?.invoke()
        onLoadingBlock?.invoke(true)

        executeAction().fold(
            success = { _ -> },
            failure = { error -> onErrorBlock?.invoke(error) }
        )

        onLoadingBlock?.invoke(false)
        onFinallyBlock?.invoke()
    }

    private suspend fun executeAction(): MRResult<R, E> {
        return try {
            action().map { mapper(it) }
        } catch (error: CancellationException) {
            throw error
        }
    }
}