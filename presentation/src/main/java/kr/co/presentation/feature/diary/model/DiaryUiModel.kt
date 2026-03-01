package kr.co.presentation.feature.diary.model

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kr.co.domain.feature.emotion.Emotion
import java.time.LocalDate


object LocalDateParceler : Parceler<LocalDate> {
    override fun create(parcel: Parcel): LocalDate {
        return LocalDate.ofEpochDay(parcel.readLong())
    }

    override fun LocalDate.write(parcel: Parcel, flags: Int) {
        parcel.writeLong(this.toEpochDay())
    }
}

@Immutable
@Parcelize
@TypeParceler<LocalDate, LocalDateParceler>()
data class DiaryUiModel(
    val id: Long = 0L,
    val date: LocalDate = LocalDate.now(),
    val title: String = "",
    val content: String = "",
    val emotion: Emotion = Emotion.UNKNOWN,
) : Parcelable