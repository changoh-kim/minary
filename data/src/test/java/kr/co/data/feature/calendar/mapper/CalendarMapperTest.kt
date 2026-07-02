package kr.co.data.feature.calendar.mapper

import kr.co.data.feature.calendar.mapper.CalendarDayMapper.toCalendarDay
import kr.co.data.feature.calendar.mapper.CalendarDayMapper.toCalendarDayModel
import kr.co.data.feature.calendar.mapper.CalendarMonthMapper.toCalendarMonth
import kr.co.data.feature.calendar.mapper.CalendarMonthMapper.toCalendarMonthModel
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CalendarMapperTest : BaseDataUnitTest() {
    @Test
    fun `maps calendar day model to domain calendar day`() {
        assertEquals(DataFixtures.calendarDay, DataFixtures.calendarDayModel.toCalendarDay())
    }

    @Test
    fun `maps domain calendar day to calendar day model`() {
        assertEquals(DataFixtures.calendarDayModel, DataFixtures.calendarDay.toCalendarDayModel())
    }

    @Test
    fun `maps calendar month model to domain calendar month`() {
        assertEquals(DataFixtures.calendarMonth, DataFixtures.calendarMonthModel.toCalendarMonth())
    }

    @Test
    fun `maps domain calendar month to calendar month model`() {
        assertEquals(DataFixtures.calendarMonthModel, DataFixtures.calendarMonth.toCalendarMonthModel())
    }
}
