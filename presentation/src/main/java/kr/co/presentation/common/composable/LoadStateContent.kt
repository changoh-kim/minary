package kr.co.presentation.common.composable

import androidx.compose.runtime.Composable
import kr.co.presentation.common.state.LoadState


@Composable
fun <T> LoadStateContent(
    loadState: LoadState<T>,
    uninitialized: @Composable () -> Unit = {},
    loading: @Composable () -> Unit = {},
    error: @Composable (Throwable?) -> Unit = {},
    content: @Composable (T) -> Unit
) {
    when (loadState) {
        is LoadState.Uninitialized -> uninitialized()
        is LoadState.Loading -> loading()
        is LoadState.Error -> error(loadState.exception)
        is LoadState.Success -> content(loadState.data)
    }
}