package kr.co.presentation.feature.account.screen.deletion

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.core.common.error.DomainError
import kr.co.core.common.logging.AppLogger
import kr.co.core.ui.common.load.load
import kr.co.core.ui.common.text.UiText
import kr.co.domain.feature.account.usecase.DeleteAccountUseCase
import kr.co.presentation.R
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.syntax.simple.blockingIntent
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@Immutable
data class AccountDeletionScreenState(
    val password: String = "",
    val isProcessing: Boolean = false,
)

@Immutable
sealed interface AccountDeletionSideEffect {
    object BackClicked : AccountDeletionSideEffect
    object DeletionSucceeded : AccountDeletionSideEffect
    data class ShowMessage(val uiText: UiText) : AccountDeletionSideEffect
}

sealed interface AccountDeletionAction {
    object BackClicked : AccountDeletionAction
    data class PasswordChanged(val newPassword: String) : AccountDeletionAction
    object DeleteAccountClicked : AccountDeletionAction
}

@OptIn(OrbitExperimental::class)
@HiltViewModel
class AccountDeletionViewModel @Inject constructor(
    private val logger: AppLogger,
    private val savedStateHandle: SavedStateHandle,
    private val deleteAccount: DeleteAccountUseCase,
) : ViewModel(), ContainerHost<AccountDeletionScreenState, AccountDeletionSideEffect> {

    private companion object {
        private const val KEY_PASSWORD = "password"
    }

    override val container = container<AccountDeletionScreenState, AccountDeletionSideEffect>(AccountDeletionScreenState())

    init {
        initState()
    }

    private fun initState() = intent {
        val savedPassword = savedStateHandle[KEY_PASSWORD] ?: ""
        reduce { state.copy(password = savedPassword) }
    }

    fun handleAction(action: AccountDeletionAction) {
        when (action) {
            is AccountDeletionAction.BackClicked -> backClicked()
            is AccountDeletionAction.PasswordChanged -> updatePassword(action.newPassword)
            is AccountDeletionAction.DeleteAccountClicked -> requestDeleteAccount()
        }
    }

    private fun backClicked() = intent {
        postSideEffect(AccountDeletionSideEffect.BackClicked)
    }

    private fun updatePassword(newPassword: String) = blockingIntent {
        reduce { state.copy(password = newPassword) }
        savedStateHandle[KEY_PASSWORD] = newPassword
    }

    private fun requestDeleteAccount() = intent {
        if (state.password.isBlank()) {
            postSideEffect(AccountDeletionSideEffect.ShowMessage(UiText.StringResource(R.string.password_is_empty)))
            return@intent
        }

        load { deleteAccount(state.password) }
            .onLoading { reduce { state.copy(isProcessing = it) } }
            .onError { handleError(it) }
            .startOnSuccess {
                postSideEffect(AccountDeletionSideEffect.DeletionSucceeded)
            }
    }

    private fun handleError(error: DomainError) = intent {
        val message = when (error) {
            DomainError.NetworkUnavailable -> R.string.network_unavailable
            DomainError.Timeout -> R.string.timeout
            DomainError.Auth.InvalidCredentials -> R.string.invalid_credentials
            DomainError.Auth.TooManyRequests -> R.string.too_many_requests
            DomainError.Auth.RequiresRecentLogin -> R.string.requires_recent_sign_in
            else -> {
                logger.e("Failed to delete account: %s", error)
                R.string.unexpected_error
            }
        }
        postSideEffect(AccountDeletionSideEffect.ShowMessage(UiText.StringResource(message)))
    }
}
