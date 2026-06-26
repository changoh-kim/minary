package kr.co.minary.logging

import kr.co.core.common.logging.AppLogger
import kr.co.minary.BuildConfig
import timber.log.Timber
import javax.inject.Inject

class TimberAppLogger @Inject constructor() : AppLogger {
    override fun v(message: String, vararg args: Any?) {
        log { location, tree -> tree.v(location.formatMessage(message), *args) }
    }

    override fun d(message: String, vararg args: Any?) {
        log { location, tree -> tree.d(location.formatMessage(message), *args) }
    }

    override fun i(message: String, vararg args: Any?) {
        log { location, tree -> tree.i(location.formatMessage(message), *args) }
    }

    override fun w(message: String, vararg args: Any?) {
        log { location, tree -> tree.w(location.formatMessage(message), *args) }
    }

    override fun w(throwable: Throwable, message: String, vararg args: Any?) {
        log { location, tree -> tree.w(throwable, location.formatMessage(message), *args) }
    }

    override fun e(message: String, vararg args: Any?) {
        log { location, tree -> tree.e(location.formatMessage(message), *args) }
    }

    override fun e(throwable: Throwable, message: String, vararg args: Any?) {
        log { location, tree -> tree.e(throwable, location.formatMessage(message), *args) }
    }

    private fun log(block: (CallerLocation, Timber.Tree) -> Unit) {
        val location = createCallerLocation()
        block(location, timber(location))
    }

    private fun timber(location: CallerLocation): Timber.Tree =
        if (BuildConfig.DEBUG) {
            Timber.tag(location.tag)
        } else {
            Timber
        }

    private companion object {
        const val DEFAULT_TAG = "Minary"
        const val TIMBER_APP_LOGGER_CLASS_NAME = "kr.co.minary.logging.TimberAppLogger"
        const val MINARY_DEBUG_TREE_CLASS_NAME = "kr.co.minary.logging.MinaryDebugTree"
        const val TIMBER_PACKAGE_NAME = "timber.log."
        const val THREAD_CLASS_NAME = "java.lang.Thread"
        const val VM_STACK_CLASS_NAME = "dalvik.system.VMStack"
    }

    internal fun createCallerLocation(
        stackTrace: Array<StackTraceElement> = Thread.currentThread().stackTrace,
    ): CallerLocation =
        stackTrace
            .firstOrNull { it.isAppLoggerCaller() }
            ?.toCallerLocation()
            ?: CallerLocation(
                className = DEFAULT_TAG,
                methodName = "unknown",
                lineNumber = 0,
            )

    private fun StackTraceElement.isAppLoggerCaller(): Boolean {
        val className = className
        return !className.isLoggerImplementationClass() &&
                !className.startsWith(TIMBER_PACKAGE_NAME) &&
                className != THREAD_CLASS_NAME &&
                className != VM_STACK_CLASS_NAME
    }

    private fun String.isLoggerImplementationClass(): Boolean =
        this == TIMBER_APP_LOGGER_CLASS_NAME ||
            startsWith("$TIMBER_APP_LOGGER_CLASS_NAME$") ||
            this == MINARY_DEBUG_TREE_CLASS_NAME ||
            startsWith("$MINARY_DEBUG_TREE_CLASS_NAME$")

    private fun StackTraceElement.toCallerLocation(): CallerLocation {
        val simpleClassName = className
            .substringAfterLast('.')
            .substringBefore('$')
        return CallerLocation(
            className = simpleClassName,
            methodName = methodName,
            lineNumber = lineNumber,
        )
    }

    internal data class CallerLocation(
        private val className: String,
        private val methodName: String,
        private val lineNumber: Int,
    ) {
        val tag: String = className

        fun formatMessage(message: String): String =
            if (BuildConfig.DEBUG) {
                "$className.$methodName():$lineNumber\n$message"
            } else {
                message
            }
    }
}
