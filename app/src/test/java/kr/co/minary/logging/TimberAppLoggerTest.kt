package kr.co.minary.logging

import android.util.Log
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import timber.log.Timber

class TimberAppLoggerTest {

    private val logger = TimberAppLogger()
    private val tree = CapturingTree()

    @BeforeEach
    fun setUp() {
        Timber.uprootAll()
        Timber.plant(tree)
    }

    @AfterEach
    fun tearDown() {
        Timber.uprootAll()
    }

    @Test
    fun debugTagUsesActualCallerForMessageLog() {
        val expected = logFromMessageHelper("Failed to test message")

        assertEquals(expected, tree.lastTag)
        assertEquals("TimberAppLoggerTest.logFromMessageHelper():${tree.lastLineNumber}\nFailed to test message", tree.lastMessage)
    }

    @Test
    fun debugTagUsesActualCallerForThrowableLog() {
        val expected = logFromThrowableHelper("Failed to test throwable")

        assertEquals(expected, tree.lastTag)
        assertTrue(
            requireNotNull(tree.lastMessage).startsWith(
                "TimberAppLoggerTest.logFromThrowableHelper():${tree.lastLineNumber}\nFailed to test throwable",
            ),
        )
    }

    @Test
    fun debugTagUsesActualCallerForAllSupportedLevels() {
        val expectedTags = logFromLevelHelper()

        assertEquals(expectedTags, tree.tags)
    }

    @Test
    fun debugTagSkipsAndroidRuntimeStackFrames() {
        val location = logger.createCallerLocation(
            arrayOf(
                StackTraceElement(
                    "dalvik.system.VMStack",
                    "getThreadStackTrace",
                    "VMStack.java",
                    -2,
                ),
                StackTraceElement(
                    "java.lang.Thread",
                    "getStackTrace",
                    "Thread.java",
                    1841,
                ),
                StackTraceElement(
                    "kr.co.minary.logging.TimberAppLogger",
                    "createCallerTag",
                    "TimberAppLogger.kt",
                    55,
                ),
                StackTraceElement(
                    "kr.co.data.feature.dashboard.repository.DashboardRepositoryImpl",
                    "getDashboard",
                    "DashboardRepositoryImpl.kt",
                    32,
                ),
            ),
        )

        assertEquals("DashboardRepositoryImpl", location.tag)
        assertEquals("DashboardRepositoryImpl.getDashboard():32\nmessage", location.formatMessage("message"))
    }

    private fun logFromMessageHelper(message: String): String {
        val expectedLine = Throwable().stackTrace[0].lineNumber + 1
        logger.e(message)
        tree.lastLineNumber = expectedLine
        return "TimberAppLoggerTest"
    }

    private fun logFromThrowableHelper(message: String): String {
        val expectedLine = Throwable().stackTrace[0].lineNumber + 1
        logger.e(IllegalStateException("test"), message)
        tree.lastLineNumber = expectedLine
        return "TimberAppLoggerTest"
    }

    private fun logFromLevelHelper(): List<String> {
        val expectedTags = mutableListOf<String>()

        expectedTags += "TimberAppLoggerTest"
        logger.v("verbose message")

        expectedTags += "TimberAppLoggerTest"
        logger.d("debug message")

        expectedTags += "TimberAppLoggerTest"
        logger.i("info message")

        expectedTags += "TimberAppLoggerTest"
        logger.w("warning message")

        expectedTags += "TimberAppLoggerTest"
        logger.w(IllegalStateException("test"), "warning throwable")

        expectedTags += "TimberAppLoggerTest"
        logger.e("error message")

        expectedTags += "TimberAppLoggerTest"
        logger.e(IllegalStateException("test"), "error throwable")

        return expectedTags
    }

    private class CapturingTree : Timber.Tree() {
        val tags = mutableListOf<String>()
        val messages = mutableListOf<String>()
        var lastLineNumber: Int = 0
        val lastTag: String?
            get() = tags.lastOrNull()
        val lastMessage: String?
            get() = messages.lastOrNull()

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            assertNotNull(tag)
            tags += requireNotNull(tag)
            messages += message
            assert(priority in Log.VERBOSE..Log.ASSERT)
        }
    }
}
