package kr.co.domain.model.calendar


sealed interface CalendarItem {
    sealed interface ContentType {
        data object YEAR : ContentType
        data object MONTH : ContentType
    }

    val key: String
    val contentType: ContentType

    val year: Int
}