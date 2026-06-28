package kr.co.core.ui.common.parceler

import android.os.Parcel
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class LocalDateParcelerInstrumentedTest {

    @Test
    fun parcelerRoundTripsLocalDateEpochDay() {
        val parcel = Parcel.obtain()
        val expected = LocalDate.of(2026, 6, 28)

        try {
            with(LocalDateParceler) {
                expected.write(parcel, 0)
            }
            parcel.setDataPosition(0)

            assertEquals(expected, LocalDateParceler.create(parcel))
        } finally {
            parcel.recycle()
        }
    }
}
