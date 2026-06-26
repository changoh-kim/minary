package kr.co.presentation.feature.account.screen.signin

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.core.common.error.DomainError
import kr.co.core.common.extension.TAG
import kr.co.core.ui.common.load.load
import kr.co.core.ui.common.text.UiText
import kr.co.domain.feature.account.usecase.SignInUseCase
import kr.co.presentation.R
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.syntax.simple.SimpleSyntax
import org.orbitmvi.orbit.syntax.simple.blockingIntent
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@Immutable
data class SignInScreenState(
    val isSigningIn: Boolean = false,
    val email: String = "",
    val password: String = "",
)

@Immutable
sealed interface SignInSideEffect {
    object SignInSucceeded : SignInSideEffect
    object SignUpClicked : SignInSideEffect
    data class ShowMessage(val uiText: UiText) : SignInSideEffect
}

sealed interface SignInAction {
    data class EmailChanged(val newEmail: String) : SignInAction
    data class PasswordChanged(val newPassword: String) : SignInAction
    object SignInClicked : SignInAction
    object SignUpClicked : SignInAction
}

@OptIn(OrbitExperimental::class)
@HiltViewModel
class SignInViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val signIn: SignInUseCase,
) : ViewModel(), ContainerHost<SignInScreenState, SignInSideEffect> {

    private companion object {
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
    }

    override val container = container<SignInScreenState, SignInSideEffect>(SignInScreenState())

    init {
        initState()
    }

    private fun initState() = intent {
        val savedEmail = savedStateHandle[KEY_EMAIL] ?: ""
        val savedPassword = savedStateHandle[KEY_PASSWORD] ?: ""

        reduce { state.copy(email = savedEmail, password = savedPassword) }
    }

    fun handleAction(action: SignInAction) {
        when (action) {
            is SignInAction.EmailChanged -> updateEmail(action.newEmail)
            is SignInAction.PasswordChanged -> updatePassword(action.newPassword)
            is SignInAction.SignInClicked -> requestSignIn()
            is SignInAction.SignUpClicked -> signUpClicked()
        }
    }

    private fun handleError(error: DomainError) = intent {
        val message = when (error) {
            DomainError.NetworkUnavailable -> R.string.network_unavailable
            DomainError.Timeout -> R.string.timeout
            DomainError.Auth.InvalidCredentials -> R.string.invalid_credentials
            DomainError.Auth.UserNotFound -> R.string.user_not_found
            DomainError.Auth.TooManyRequests -> R.string.too_many_requests
            else -> {
                Log.e(TAG, "Failed to sign in: $error")
                R.string.unexpected_error
            }
        }
        postSideEffect(SignInSideEffect.ShowMessage(UiText.StringResource(message)))
    }

    private fun updateEmail(newEmail: String) = blockingIntent {
        reduce { state.copy(email = newEmail) }
        savedStateHandle[KEY_EMAIL] = newEmail
    }

    private fun updatePassword(newPassword: String) = blockingIntent {
        reduce { state.copy(password = newPassword) }
        savedStateHandle[KEY_PASSWORD] = newPassword
    }

    private fun requestSignIn() = intent {
        if (!validateInput()) return@intent

        load { signIn(state.email, state.password) }
            .onLoading { isLoading -> reduce { state.copy(isSigningIn = isLoading) } }
            .onError { handleError(it) }
            .startOnSuccess {
                postSideEffect(SignInSideEffect.SignInSucceeded)
            }
    }

    private suspend fun SimpleSyntax<SignInScreenState, SignInSideEffect>.validateInput(): Boolean {
        return when {
            state.email.isBlank() -> {
                postSideEffect(SignInSideEffect.ShowMessage(UiText.StringResource(R.string.id_is_empty)))
                false
            }
            state.password.isBlank() -> {
                postSideEffect(SignInSideEffect.ShowMessage(UiText.StringResource(R.string.password_is_empty)))
                false
            }
            else -> true
        }
    }

    private fun signUpClicked() = intent { postSideEffect(SignInSideEffect.SignUpClicked) }
}
