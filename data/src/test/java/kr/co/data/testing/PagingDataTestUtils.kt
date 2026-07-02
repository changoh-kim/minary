package kr.co.data.testing

import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <T : Any> TestScope.collectPagingDataItems(
    pagingData: PagingData<T>,
): List<T> {
    val dispatcher = UnconfinedTestDispatcher(testScheduler)
    val presenter = object : PagingDataPresenter<T>(mainContext = dispatcher) {
        override suspend fun presentPagingDataEvent(event: PagingDataEvent<T>) = Unit
    }

    val job = backgroundScope.launch(dispatcher) {
        presenter.collectFrom(pagingData)
    }
    testScheduler.advanceUntilIdle()
    val items = presenter.snapshot().items
    job.cancel()

    return items
}
