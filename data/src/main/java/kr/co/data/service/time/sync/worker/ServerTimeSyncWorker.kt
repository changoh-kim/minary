package kr.co.data.service.time.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.michaelbull.result.fold
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kr.co.domain.service.time.ServerTimeProvider

@HiltWorker
class ServerTimeSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val serverTime: ServerTimeProvider,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "server_time_sync_worker"
    }

    override suspend fun doWork(): Result {
        return serverTime.sync().fold(
            success = { Result.success() },
            failure = {
                if (runAttemptCount < 3) Result.retry()
                else Result.failure()
            }
        )
    }
}