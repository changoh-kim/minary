package kr.co.core.ui.common.load

import androidx.compose.runtime.Composable
import kr.co.core.common.error.DomainError

@Composable
fun <T> LoadStateContentContainer(
    loadState: LoadState<T>,
    uninitialized: @Composable () -> Unit = {},
    loading: @Composable () -> Unit = {},
    error: @Composable (DomainError?) -> Unit = {},
    content: @Composable (T) -> Unit
) {
    when (loadState) {
        is LoadState.Uninitialized -> uninitialized()
        is LoadState.Loading -> loading()
        is LoadState.Error -> error(loadState.error)
        is LoadState.Success -> content(loadState.data)
    }
}