package kr.co.data.service.time

import android.content.Context
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.instacart.library.truetime.TrueTime
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.co.data.extension.toDomainError
import kr.co.core.common.error.DomainError
import kr.co.domain.service.time.ServerTimeProvider
import kr.co.domain.service.time.ServerTimeSyncScheduler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrueTimeProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val serverTimeSyncScheduler: ServerTimeSyncScheduler,
) : ServerTimeProvider {

    companion object {
        private const val NTP_HOST = "time.google.com"
        private const val CONNECTION_TIMEOUT = 10_000 // 10초
    }

    override suspend fun sync(): Result<Unit, DomainError> =
        runSuspendCatching {
            TrueTime.build()
                .withNtpHost(NTP_HOST)
                .withConnectionTimeout(CONNECTION_TIMEOUT)  // 10초 타임아웃
                .withSharedPreferencesCache(context)        // 오프라인 캐시 사용
                .withLoggingEnabled(true)                   // 로그 활성화 기능(BuildConfig.DEBUG 활용)
                .initialize()
        }.mapError { it.toDomainError() }

    override fun now(): Long {
        return if (TrueTime.isInitialized()) {
            TrueTime.now().time
        } else {
            serverTimeSyncScheduler.scheduleSync()
            System.currentTimeMillis()
        }
    }
}