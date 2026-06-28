package kr.co.core.database.converter

import kr.co.core.common.model.Emotion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class DatabaseConvertersTest {

    @Test
    fun `local date converter round trips epoch day`() {
        val date = LocalDate.of(2026, 6, 28)

        assertEquals(date.toEpochDay(), LocalDateConverters.toLong(date))
        assertEquals(date, LocalDateConverters.fromLong(date.toEpochDay()))
        assertNull(LocalDateConverters.toLong(null))
        assertNull(LocalDateConverters.fromLong(null))
    }

    @Test
    fun `boolean converter round trips integer value`() {
        assertEquals(1, BooleanConverters.fromBoolean(true))
        assertEquals(0, BooleanConverters.fromBoolean(false))
        assertEquals(true, BooleanConverters.toBoolean(1))
        assertEquals(false, BooleanConverters.toBoolean(0))
        assertNull(BooleanConverters.fromBoolean(null))
        assertNull(BooleanConverters.toBoolean(null))
    }

    @Test
    fun `emotion converter maps unknown strings to unknown emotion`() {
        assertEquals("JOY", EmotionConverters.toString(Emotion.JOY))
        assertEquals(Emotion.CALMNESS, EmotionConverters.fromString("calmness"))
        assertEquals(Emotion.UNKNOWN, EmotionConverters.fromString("unknown-value-test"))
        assertNull(EmotionConverters.toString(null))
        assertNull(EmotionConverters.fromString(null))
    }

    @Test
    fun `year month converter round trips iso year month string`() {
        val yearMonth = YearMonth.of(2026, 6)

        assertEquals("2026-06", YearMonthConverters.toString(yearMonth))
        assertEquals(yearMonth, YearMonthConverters.fromString("2026-06"))
        assertNull(YearMonthConverters.toString(null))
        assertNull(YearMonthConverters.fromString(null))
    }
}
