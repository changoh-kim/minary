package kr.co.data.testing

import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.ForegroundUpdater
import androidx.work.ProgressUpdater
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import androidx.work.impl.utils.taskexecutor.SerialExecutor
import androidx.work.impl.utils.taskexecutor.TaskExecutor
import com.google.common.util.concurrent.ListenableFuture
import java.util.UUID
import java.util.concurrent.Executor

fun workerParameters(runAttemptCount: Int = 0): WorkerParameters {
    val directExecutor = Executor { command -> command.run() }
    val serialExecutor = object : SerialExecutor {
        override fun execute(command: Runnable) {
            command.run()
        }

        override fun hasPendingTasks(): Boolean = false
    }
    val taskExecutor = object : TaskExecutor {
        override fun getMainThreadExecutor(): Executor = directExecutor
        override fun getSerialTaskExecutor(): SerialExecutor = serialExecutor
    }
    val progressUpdater = ProgressUpdater { _, _, _ ->
        immediateVoidFuture()
    }
    val foregroundUpdater = ForegroundUpdater { _, _, _: ForegroundInfo ->
        immediateVoidFuture()
    }

    return WorkerParameters(
        UUID.randomUUID(),
        Data.EMPTY,
        emptyList(),
        WorkerParameters.RuntimeExtras(),
        runAttemptCount,
        0,
        directExecutor,
        taskExecutor,
        WorkerFactory.getDefaultWorkerFactory(),
        progressUpdater,
        foregroundUpdater,
    )
}

private fun immediateVoidFuture(): ListenableFuture<Void> =
    SettableFuture.create<Void>().apply { set(null) }
