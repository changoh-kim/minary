package kr.co.data.feature.calendar.mapper

import kr.co.data.feature.calendar.model.CalendarDayModel
import kr.co.domain.feature.calendar.model.CalendarDay

object CalendarDayMapper {
    fun CalendarDayModel.toCalendarDay(): CalendarDay {
        return CalendarDay(
            date = date,
            isCurrentMonth = isCurrentMonth,
            diary = diary
        )
    }

    fun CalendarDay.toCalendarDayModel(): CalendarDayModel {
        return CalendarDayModel(
            date = date,
            isCurrentMonth = isCurrentMonth,
            diary = diary
        )
    }
}