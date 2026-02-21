package kr.co.presentation.common.extension

import androidx.annotation.CheckResult
import kotlinx.coroutines.CancellationException
import kr.co.presentation.common.state.LoadState
import org.orbitmvi.orbit.syntax.simple.SimpleSyntax

/**
 * Orbit MVI의 `SimpleSyntax` 내에서 안전한 비동기 작업을 시작하기 위한 확장 함수입니다.
 * `Result`를 반환하는 `action`을 받아, 체이닝 가능한 `OrbitCall`를 생성합니다.
 *
 * @param STATE Orbit의 State 타입
 * @param SIDE_EFFECT Orbit의 SideEffect 타입
 * @param T `action`이 성공했을 때 반환하는 데이터의 타입
 * @param action 비동기적으로 실행될 작업으로, `Result<T>`를 반환해야 합니다.
 * @return `OrbitCall` 인스턴스
 */
@CheckResult(suggest = "launchOnSuccess 또는 launchAsLoadState 호출하여 작업을 완료해야 합니다.")
fun <STATE : Any, SIDE_EFFECT : Any, T : Any> SimpleSyntax<STATE, SIDE_EFFECT>.safeCall(
    action: suspend () -> Result<T>
): OrbitCall<STATE, SIDE_EFFECT, T, T> {
    return OrbitCall(this, action) { it }
}

/**
 * Orbit MVI의 비동기 작업을 위한 빌더 클래스입니다.
 * 로딩, 성공, 실패 상태를 관리하며, `onStart`, `onFinally`, `onError`, `onLoading`과 같은 콜백을 체이닝 방식으로 구성할 수 있습니다.
 * 작업 실행을 위해서는 반드시 `launchAsLoadState` 또는 `launchOnSuccess`와 같은 종단 연산자를 호출해야 합니다.
 */
class OrbitCall<STATE : Any, SIDE_EFFECT : Any, T : Any, R : Any>(
    private val syntax: SimpleSyntax<STATE, SIDE_EFFECT>,
    private val action: suspend () -> Result<T>,
    private val mapper: suspend (T) -> R
) {
    private var onStartBlock: (suspend SimpleSyntax<STATE, SIDE_EFFECT>.() -> Unit)? = null
    private var onFinallyBlock: (suspend SimpleSyntax<STATE, SIDE_EFFECT>.() -> Unit)? = null
    private var onLoadingBlock: (suspend SimpleSyntax<STATE, SIDE_EFFECT>.(Boolean) -> Unit)? = null
    private var onErrorBlock: (suspend SimpleSyntax<STATE, SIDE_EFFECT>.(Throwable) -> Unit)? = null

    /**
     * 작업 성공 시의 결과를 다른 타입으로 변환합니다.
     * 이 함수를 사용한 후에도 반드시 종단 연산자를 호출해야 합니다.
     *
     * @param newMapper `R` 타입을 `NEW_R` 타입으로 변환하는 매퍼 함수
     * @return 새로운 결과 타입을 가진 `OrbitCall` 인스턴스
     */
    @CheckResult
    fun <NEW_R : Any> map(newMapper: suspend (R) -> NEW_R): OrbitCall<STATE, SIDE_EFFECT, T, NEW_R> {
        return OrbitCall(syntax, action) { result ->
            newMapper(mapper(result))
        }.also { newBuilder ->
            // 상태 복사
            newBuilder.onStartBlock = this.onStartBlock
            newBuilder.onFinallyBlock = this.onFinallyBlock
            newBuilder.onErrorBlock = this.onErrorBlock
            newBuilder.onLoadingBlock = this.onLoadingBlock
        }
    }

    /**
     * 작업이 시작될 때 호출될 콜백을 등록합니다.
     * 이 함수를 사용한 후에도 반드시 종단 연산자를 호출해야 합니다.
     */
    @CheckResult
    fun onStart(block: suspend SimpleSyntax<STATE, SIDE_EFFECT>.() -> Unit) = apply {
        val oldBlock = this.onStartBlock
        this.onStartBlock = if (oldBlock == null) block else {
            { oldBlock(); block() }
        }
    }

    /**
     * 작업이 성공 또는 실패로 완료된 후 항상 호출될 콜백을 등록합니다.
     * 이 함수를 사용한 후에도 반드시 종단 연산자를 호출해야 합니다.
     */
    @CheckResult
    fun onFinally(block: suspend SimpleSyntax<STATE, SIDE_EFFECT>.() -> Unit) = apply {
        val oldBlock = this.onFinallyBlock
        this.onFinallyBlock = if (oldBlock == null) block else {
            { oldBlock(); block() }
        }
    }

    /**
     * 작업의 로딩 상태가 변경될 때 호출될 콜백을 등록합니다.
     * 종단 연산자에서 작업이 시작될 때 `true`, 작업이 완료(성공/실패)될 때 `false`를 전달받습니다.
     * 이 함수를 사용한 후에도 반드시 종단 연산자를 호출해야 합니다.
     */
    @CheckResult
    fun onLoading(block: suspend SimpleSyntax<STATE, SIDE_EFFECT>.(isLoading: Boolean) -> Unit) = apply {
        val oldBlock = this.onLoadingBlock
        this.onLoadingBlock =
            if (oldBlock == null) block else { isLoading -> oldBlock(isLoading); block(isLoading) }
    }

    /**
     * 작업이 실패했을 때 호출될 콜백을 등록합니다.
     * 이 함수를 사용한 후에도 반드시 종단 연산자를 호출해야 합니다.
     */
    @CheckResult
    fun onError(block: suspend SimpleSyntax<STATE, SIDE_EFFECT>.(error: Throwable) -> Unit) = apply {
        val oldBlock = this.onErrorBlock
        this.onErrorBlock =
            if (oldBlock == null) block else { error -> oldBlock(error); block(error) }
    }

    /**
     * [종단 연산자]
     * 이 함수를 호출해야 요청이 실행되고 결과가 `LoadState`로 반영됩니다.
     *
     * @param onLoadStateBlock `LoadState` 변경을 처리할 콜백
     */
    suspend fun launchAsLoadState(
        onLoadStateBlock: suspend SimpleSyntax<STATE, SIDE_EFFECT>.(loadState: LoadState<R>) -> Unit
    ) {
        with(syntax) {
            onStartBlock?.invoke(this) // onStart 실행
            onLoadingBlock?.invoke(this, true)
            onLoadStateBlock(LoadState.Loading)

            executeAction()
                .fold(
                    onSuccess = { data -> onLoadStateBlock(LoadState.Success(data)) },
                    onFailure = { error ->
                        onLoadStateBlock(LoadState.Error(error))
                        onErrorBlock?.invoke(this, error) // onError 실행
                    }
                )

            onLoadingBlock?.invoke(this, false)
            onFinallyBlock?.invoke(this) // onFinally 실행
        }
    }

    /**
     * [종단 연산자]
     * 이 함수를 호출해야 요청이 실행되고 성공 시의 결과만 처리됩니다.
     *
     * @param onSuccessBlock 작업 성공 시 호출될 콜백
     */
    suspend fun launchOnSuccess(
        onSuccessBlock: suspend SimpleSyntax<STATE, SIDE_EFFECT>.(R) -> Unit
    ) {
        with(syntax) {
            onStartBlock?.invoke(this)
            onLoadingBlock?.invoke(this, true)

            executeAction()
                .fold(
                    onSuccess = { data -> onSuccessBlock(data) },
                    onFailure = { error -> onErrorBlock?.invoke(this, error) }
                )

            onLoadingBlock?.invoke(this, false)
            onFinallyBlock?.invoke(this)
        }
    }

    private suspend fun executeAction(): Result<R> {
        return try {
            action().mapCatching { mapper(it) }
        } catch (error: CancellationException) {
            // 코루틴의 협력적 취소를 지원하기 위해 CancellationException은 잡은 후 다시 던집니다.
            // 이 예외를 무시하면 코루틴이 취소되었을 때 상위 코루틴으로 전파되지 않아,
            // 원하지 않는 동작이나 리소스 누수를 유발할 수 있습니다.
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}