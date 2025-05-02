package kr.co.presentation.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.presentation.ui.screen.NavigationItem
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@Immutable
data class MainContainerState(
    val selectedItem: String = mutableStateOf(NavigationItem.Calender.route).toString()
)

sealed class MainContainerSideEffect {
    data class ShowMsg(val msg: String) : MainContainerSideEffect()
}

@HiltViewModel
class MainContainerViewModel @Inject constructor(

) : ViewModel(), ContainerHost<MainContainerState, MainContainerSideEffect> {
    override val container =
        container<MainContainerState, MainContainerSideEffect>(MainContainerState())
}