package kr.co.presentation.main.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.presentation.common.model.UiText
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import javax.annotation.concurrent.Immutable
import javax.inject.Inject


@Immutable
sealed interface MainSideEffect {
    data class NavigateToDiaryScreen(val date: LocalDate) : MainSideEffect
    data class ShowMsg(val uiText: UiText) : MainSideEffect
}

sealed interface MainIntent {
    data class NavigateToDiaryScreen(val date: LocalDate) : MainIntent
}

@HiltViewModel
class MainViewModel @Inject constructor(
) : ViewModel(), ContainerHost<Unit, MainSideEffect> {
    override val container = container<Unit, MainSideEffect>(Unit)

    companion object {}

    fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.NavigateToDiaryScreen -> navigateToPreviewDiaryScreen(intent.date)
        }
    }

    private fun navigateToPreviewDiaryScreen(date: LocalDate) = intent {
        postSideEffect(MainSideEffect.NavigateToDiaryScreen(date))
    }
}