package kr.co.presentation.ui.model.calendar

import androidx.compose.runtime.Immutable


@Immutable
interface BaseItem {

    @Immutable
    sealed interface ContentType {
        data object YEAR : ContentType
        data object MONTH : ContentType
        data object DATE : ContentType
    }

    val key: String
    val contentType: ContentType
}