package kr.co.domain.feature.calendar.model

import kr.co.domain.feature.diary.model.Diary
import java.time.LocalDate


data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val diary: Diary? = null
)