package kr.co.domain.extention

import java.time.LocalDate
import java.time.format.DateTimeFormatter


fun LocalDate.dateToString(): String = this.format(DateTimeFormatter.ISO_LOCAL_DATE)
fun String.toLocalDate(): LocalDate = LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)