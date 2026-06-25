package kr.co.presentation.feature.calendar.model

import androidx.compose.runtime.Immutable

@Immutable
interface CalendarBaseItem {

    @Immutable
    sealed interface ContentType {
        object TypeYear : ContentType
        object TypeMonth : ContentType
        object TypeDay : ContentType
    }

    val key: String
    val contentType: ContentType
}