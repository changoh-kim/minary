package kr.co.core.common.extension

import kr.co.core.common.model.AppTheme
import kr.co.core.common.model.Emotion
import kr.co.core.common.model.Gender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ConverterExtensionsTest {

    @Test
    fun `converts valid date strings and epoch days to local date`() {
        val date = LocalDate.of(2026, 6, 28)

        assertEquals(date, "2026-06-28".toLocalDate())
        assertEquals(date, date.toEpochDay().toLocalDate())
    }

    @Test
    fun `returns null for invalid date inputs`() {
        val nullString: String? = null
        val nullLong: Long? = null

        assertNull(nullString.toLocalDate())
        assertNull(nullLong.toLocalDate())
        assertNull("".toLocalDate())
        assertNull("invalid-date-test".toLocalDate())
        assertNull(Long.MAX_VALUE.toLocalDate())
    }

    @Test
    fun `converts strings to enum values with fallback`() {
        assertEquals(Emotion.CALMNESS, " calmness ".toEmotion())
        assertEquals(Emotion.UNKNOWN, "unknown-value-test".toEmotion())
        assertEquals(Gender.FEMALE, "female".toGender())
        assertEquals(Gender.NONE, "unknown-value-test".toGender())
        assertEquals(AppTheme.DARK, "dark".toAppTheme())
        assertEquals(AppTheme.SYSTEM, "unknown-value-test".toAppTheme())
    }

    @Test
    fun `converts boolean and int values`() {
        assertEquals(1, true.toInt())
        assertEquals(0, false.toInt())
        assertTrue(1.toBoolean())
        assertFalse(0.toBoolean())
        assertFalse(2.toBoolean())
    }
}
