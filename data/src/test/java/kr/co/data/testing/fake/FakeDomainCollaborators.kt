package kr.co.data.testing.fake

import com.github.michaelbull.result.Ok
import kr.co.core.common.logging.AppLogger
import kr.co.core.common.result.AppResult
import kr.co.domain.feature.diary.sync.DiarySyncManager
import kr.co.domain.feature.diary.sync.DiarySyncScheduler
import kr.co.domain.feature.profile.sync.UserProfileSyncScheduler
import kr.co.domain.feature.setting.sync.UserSettingsSyncScheduler
import kr.co.domain.service.image.ImageProcessor
import kr.co.domain.service.time.ServerTimeProvider
import java.time.YearMonth

class FakeAppLogger : AppLogger {
    val verboseMessages = mutableListOf<String>()
    val debugMessages = mutableListOf<String>()
    val infoMessages = mutableListOf<String>()
    val warningMessages = mutableListOf<String>()
    val warningThrowables = mutableListOf<Pair<Throwable, String>>()
    val errorMessages = mutableListOf<String>()
    val errorThrowables = mutableListOf<Pair<Throwable, String>>()

    override fun v(message: String, vararg args: Any?) {
        verboseMessages += message.formatArgs(args)
    }

    override fun d(message: String, vararg args: Any?) {
        debugMessages += message.formatArgs(args)
    }

    override fun i(message: String, vararg args: Any?) {
        infoMessages += message.formatArgs(args)
    }

    override fun w(message: String, vararg args: Any?) {
        warningMessages += message.formatArgs(args)
    }

    override fun w(throwable: Throwable, message: String, vararg args: Any?) {
        warningThrowables += throwable to message.formatArgs(args)
    }

    override fun e(message: String, vararg args: Any?) {
        errorMessages += message.formatArgs(args)
    }

    override fun e(throwable: Throwable, message: String, vararg args: Any?) {
        errorThrowables += throwable to message.formatArgs(args)
    }

    private fun String.formatArgs(args: Array<out Any?>): String =
        if (args.isEmpty()) this else format(*args)
}

class FakeImageProcessor : ImageProcessor {
    val resizeRequests = mutableListOf<ResizeImageRequest>()
    var resizeResult: AppResult<String> = Ok("/tmp/resized-profile-photo-test.jpg")

    override suspend fun resizeImage(
        sourceUrl: String,
        targetUrl: String,
        maxWidth: Int,
        maxHeight: Int,
    ): AppResult<String> {
        resizeRequests += ResizeImageRequest(sourceUrl, targetUrl, maxWidth, maxHeight)
        return resizeResult
    }
}

data class ResizeImageRequest(
    val sourceUrl: String,
    val targetUrl: String,
    val maxWidth: Int,
    val maxHeight: Int,
)

class FakeDiarySyncScheduler : DiarySyncScheduler {
    val calls = mutableListOf<String>()

    override fun scheduleFullSync() {
        calls += "scheduleFullSync"
    }

    override fun rescheduleFullSync() {
        calls += "rescheduleFullSync"
    }

    override fun scheduleImmediateSync() {
        calls += "scheduleImmediateSync"
    }

    override fun schedulePeriodicSync() {
        calls += "schedulePeriodicSync"
    }

    override fun cancelFullSync() {
        calls += "cancelFullSync"
    }

    override fun cancelImmediateSync() {
        calls += "cancelImmediateSync"
    }

    override fun cancelPeriodicSync() {
        calls += "cancelPeriodicSync"
    }

    override fun cancelAllSync() {
        calls += "cancelAllSync"
    }
}

class FakeDiarySyncManager : DiarySyncManager {
    val chunkedSyncUids = mutableListOf<String>()
    val immediatePushUids = mutableListOf<String>()
    val monthSyncRequests = mutableListOf<Pair<String, YearMonth>>()
    val initialPullUids = mutableListOf<String>()
    var chunkedSyncResult: AppResult<Boolean> = Ok(false)
    var immediatePushResult: AppResult<Unit> = Ok(Unit)
    var monthSyncResult: AppResult<Unit> = Ok(Unit)
    var initialPullResult: AppResult<Unit> = Ok(Unit)
    var initialPullProgressValues: List<Float> = emptyList()

    override suspend fun performChunkedSync(userId: String): AppResult<Boolean> {
        chunkedSyncUids += userId
        return chunkedSyncResult
    }

    override suspend fun performImmediatePush(userId: String): AppResult<Unit> {
        immediatePushUids += userId
        return immediatePushResult
    }

    override suspend fun performMonthSync(userId: String, yearMonth: YearMonth): AppResult<Unit> {
        monthSyncRequests += userId to yearMonth
        return monthSyncResult
    }

    override suspend fun performInitialPull(
        userId: String,
        onProgress: (Float) -> Unit,
    ): AppResult<Unit> {
        initialPullUids += userId
        initialPullProgressValues.forEach(onProgress)
        return initialPullResult
    }
}

class FakeUserProfileSyncScheduler : UserProfileSyncScheduler {
    val calls = mutableListOf<String>()

    override fun scheduleProfilePush() {
        calls += "scheduleProfilePush"
    }

    override fun scheduleProfilePhotoPush() {
        calls += "scheduleProfilePhotoPush"
    }

    override fun cancelProfilePush() {
        calls += "cancelProfilePush"
    }

    override fun cancelProfilePhotoPush() {
        calls += "cancelProfilePhotoPush"
    }

    override fun cancelAll() {
        calls += "cancelAll"
    }
}

class FakeUserSettingsSyncScheduler : UserSettingsSyncScheduler {
    val calls = mutableListOf<String>()

    override fun scheduleSettingsPush() {
        calls += "scheduleSettingsPush"
    }

    override fun cancelSettingsPush() {
        calls += "cancelSettingsPush"
    }

    override fun cancelAll() {
        calls += "cancelAll"
    }
}

class FakeServerTimeProvider(
    var now: Long = 0L,
) : ServerTimeProvider {
    var syncResult: AppResult<Unit> = Ok(Unit)
    var syncCallCount = 0

    override suspend fun sync(): AppResult<Unit> {
        syncCallCount++
        return syncResult
    }

    override fun now(): Long = now
}
