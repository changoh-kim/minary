package kr.co.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.co.domain.usecase.IsUserLoggedInUseCase
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase
) : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(false)
//    val isLoggedIn : StateFlow<Boolean> get() = _isLoggedIn
    val isLoggedIn : StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            _isLoggedIn.value = isUserLoggedInUseCase()
        }
    }
}