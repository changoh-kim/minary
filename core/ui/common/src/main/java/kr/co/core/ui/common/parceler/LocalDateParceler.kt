package kr.co.core.ui.common.parceler

import android.os.Parcel
import kotlinx.parcelize.Parceler
import java.time.LocalDate

object LocalDateParceler : Parceler<LocalDate> {
    override fun create(parcel: Parcel): LocalDate {
        return LocalDate.ofEpochDay(parcel.readLong())
    }

    override fun LocalDate.write(parcel: Parcel, flags: Int) {
        parcel.writeLong(this.toEpochDay())
    }
}