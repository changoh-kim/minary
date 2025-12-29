package kr.co.domain.model.calendar


data class YearData(
    override val year: Int,
    override val key: String = year.toString(),
    override val contentType: CalendarItem.ContentType = CalendarItem.ContentType.YEAR
) : CalendarItem