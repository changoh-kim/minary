package kr.co.presentation.viewmodel.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.presentation.ui.model.UiText
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.annotation.concurrent.Immutable
import javax.inject.Inject


@Immutable
data class MainState(
    val selectedItem: String = ""
)

sealed class MainSideEffect {
    data class ShowMsg(val uiText: UiText) : MainSideEffect()
}

@HiltViewModel
class MainViewModel @Inject constructor(

) : ViewModel(), ContainerHost<MainState, MainSideEffect> {
    override val container =
        container<MainState, MainSideEffect>(MainState())
}