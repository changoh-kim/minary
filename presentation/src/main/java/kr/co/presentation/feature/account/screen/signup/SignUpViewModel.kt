package kr.co.presentation.feature.account.screen.signup

import kr.co.core.common.logging.AppLogger
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.core.common.error.DomainError
import kr.co.core.common.extension.toLocalDate
import kr.co.core.common.model.Gender
import kr.co.core.ui.common.load.load
import kr.co.core.ui.common.text.UiText
import kr.co.domain.feature.account.model.SignUpInfo
import kr.co.domain.feature.account.usecase.CheckEmailAvailabilityUseCase
import kr.co.domain.feature.account.usecase.CreateAccountUseCase
import kr.co.presentation.R
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.syntax.simple.SimpleSyntax
import org.orbitmvi.orbit.syntax.simple.blockingIntent
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@Immutable
data class SignUpScreenState(
    val isSigningUp: Boolean = false,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val name: String = "",
    val gender: Gender = Gender.NONE,
    val birthday: LocalDate = LocalDate.now(),
    val address: String = "",
    val phoneNumber: String = "",
    val isEmailAvailable: Boolean? = null,
    val isCheckingEmail: Boolean = false,
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val confirmPasswordError: UiText? = null,
    val nameError: UiText? = null,
    val addressError: UiText? = null,
    val phoneNumberError: UiText? = null,
)

@Immutable
sealed interface SignUpSideEffect {
    object SignUpSucceeded : SignUpSideEffect
    data class ShowMessage(val uiText: UiText) : SignUpSideEffect
}

sealed interface SignUpAction {
    data class EmailChanged(val newEmail: String) : SignUpAction
    data class PasswordChanged(val newPassword: String) : SignUpAction
    data class ConfirmPasswordChanged(val newConfirmPassword: String) : SignUpAction
    data class NameChanged(val newName: String) : SignUpAction
    data class GenderChanged(val newGender: Gender) : SignUpAction
    data class BirthdayChanged(val newBirthday: LocalDate) : SignUpAction
    data class AddressChanged(val newAddress: String) : SignUpAction
    data class PhoneNumberChanged(val newPhoneNumber: String) : SignUpAction
    object EmailCheckClicked : SignUpAction
    object SignUpClicked : SignUpAction
}

@OptIn(OrbitExperimental::class)
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val logger: AppLogger,
    private val savedStateHandle: SavedStateHandle,
    private val createAccount: CreateAccountUseCase,
    private val checkEmailAvailabilityUseCase: CheckEmailAvailabilityUseCase,
) : ViewModel(), ContainerHost<SignUpScreenState, SignUpSideEffect> {

    private companion object {
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_CONFIRM_PASSWORD = "confirm_password"
        private const val KEY_NAME = "name"
        private const val KEY_GENDER = "gender"
        private const val KEY_BIRTHDAY = "birthday"
        private const val KEY_ADDRESS = "address"
        private const val KEY_PHONE_NUMBER = "phone_number"

        private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
        private val PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$".toRegex()
        private val PHONE_REGEX = "^\\d{10,11}$".toRegex()
    }

    override val container = container<SignUpScreenState, SignUpSideEffect>(SignUpScreenState())

    init {
        initState()
    }

    private fun initState() = intent {
        val savedEmail = savedStateHandle[KEY_EMAIL] ?: ""
        val savedPassword = savedStateHandle[KEY_PASSWORD] ?: ""
        val savedConfirmPassword = savedStateHandle[KEY_CONFIRM_PASSWORD] ?: ""
        val savedName = savedStateHandle[KEY_NAME] ?: ""
        val savedGender = savedStateHandle[KEY_GENDER] ?: Gender.NONE
        val savedBirthday = savedStateHandle.get<String>(KEY_BIRTHDAY)
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { it.toLocalDate() }.getOrNull() }
            ?: state.birthday
        val savedAddress = savedStateHandle[KEY_ADDRESS] ?: ""
        val savedPhoneNumber = savedStateHandle[KEY_PHONE_NUMBER] ?: ""

        reduce {
            state.copy(
                email = savedEmail,
                password = savedPassword,
                confirmPassword = savedConfirmPassword,
                name = savedName,
                gender = savedGender,
                birthday = savedBirthday,
                address = savedAddress,
                phoneNumber = savedPhoneNumber,
            )
        }
    }

    fun handleAction(action: SignUpAction) {
        when (action) {
            is SignUpAction.EmailChanged -> updateEmail(action.newEmail)
            is SignUpAction.PasswordChanged -> updatePassword(action.newPassword)
            is SignUpAction.ConfirmPasswordChanged -> updateConfirmPassword(action.newConfirmPassword)
            is SignUpAction.NameChanged -> updateName(action.newName)
            is SignUpAction.GenderChanged -> updateGender(action.newGender)
            is SignUpAction.BirthdayChanged -> updateBirthday(action.newBirthday)
            is SignUpAction.AddressChanged -> updateAddress(action.newAddress)
            is SignUpAction.PhoneNumberChanged -> updatePhoneNumber(action.newPhoneNumber)
            is SignUpAction.EmailCheckClicked -> checkEmailAvailability()
            is SignUpAction.SignUpClicked -> requestSignUp()
        }
    }

    private fun handleSingUpError(error: DomainError) = intent {
        val message = when (error) {
            DomainError.NetworkUnavailable -> R.string.network_unavailable
            DomainError.Timeout -> R.string.timeout
            DomainError.Auth.InvalidCredentials -> R.string.invalid_credentials
            DomainError.Auth.EmailAlreadyInUse -> R.string.email_already_in_use
            DomainError.Auth.WeakPassword -> R.string.weak_password
            DomainError.Auth.TooManyRequests -> R.string.too_many_requests
            DomainError.Store.PermissionDenied -> {
                logger.e("Failed to signup: Permission denied")
                R.string.unexpected_error
            }
            else -> {
                logger.e("Failed to signup: %s", error)
                R.string.unexpected_error
            }
        }
        postSideEffect(SignUpSideEffect.ShowMessage(UiText.StringResource(message)))
    }

    private fun updateEmail(newEmail: String) = blockingIntent {
        reduce { state.copy(email = newEmail, isEmailAvailable = null, emailError = null) }
        savedStateHandle[KEY_EMAIL] = newEmail
    }

    private fun updatePassword(newPassword: String) = blockingIntent {
        reduce { state.copy(password = newPassword, passwordError = null) }
        savedStateHandle[KEY_PASSWORD] = newPassword
    }

    private fun updateConfirmPassword(newConfirmPassword: String) = blockingIntent {
        reduce { state.copy(confirmPassword = newConfirmPassword, confirmPasswordError = null) }
        savedStateHandle[KEY_CONFIRM_PASSWORD] = newConfirmPassword
    }

    private fun updateName(newName: String) = blockingIntent {
        reduce { state.copy(name = newName, nameError = null) }
        savedStateHandle[KEY_NAME] = newName
    }

    private fun updateGender(newGender: Gender) = intent {
        reduce { state.copy(gender = newGender) }
        savedStateHandle[KEY_GENDER] = newGender
    }

    private fun updateBirthday(newBirthday: LocalDate) = intent {
        reduce { state.copy(birthday = newBirthday) }
        savedStateHandle[KEY_BIRTHDAY] = newBirthday.toString()
    }

    private fun updateAddress(newAddress: String) = blockingIntent {
        reduce { state.copy(address = newAddress, addressError = null) }
        savedStateHandle[KEY_ADDRESS] = newAddress
    }

    private fun updatePhoneNumber(newPhoneNumber: String) = blockingIntent {
        reduce { state.copy(phoneNumber = newPhoneNumber, phoneNumberError = null) }
        savedStateHandle[KEY_PHONE_NUMBER] = newPhoneNumber
    }

    private fun checkEmailAvailability() = intent {
        if (state.email.isBlank()) {
            postSideEffect(SignUpSideEffect.ShowMessage(UiText.StringResource(R.string.email_is_empty)))
            return@intent
        }

        if (!EMAIL_REGEX.matches(state.email)) {
            reduce { state.copy(emailError = UiText.StringResource(R.string.invalid_email_format)) }
            return@intent
        }

        load { checkEmailAvailabilityUseCase(state.email) }
            .onLoading { isLoading -> reduce { state.copy(isCheckingEmail = isLoading) } }
            .onError { handleSingUpError(it) }
            .startOnSuccess { isAvailable ->
                reduce { state.copy(isEmailAvailable = isAvailable) }
            }
    }

    private fun requestSignUp() = intent {
        if (!validateInput()) return@intent

        val signUpInfo = SignUpInfo(
            email = state.email,
            password = state.password,
            name = state.name,
            gender = state.gender,
            birthday = state.birthday,
            address = state.address,
            phoneNumber = state.phoneNumber
        )

        load { createAccount(signUpInfo) }
            .onLoading { isLoading -> reduce { state.copy(isSigningUp = isLoading) } }
            .onError { handleSingUpError(it) }
            .startOnSuccess {
                postSideEffect(SignUpSideEffect.SignUpSucceeded)
            }
    }

    private suspend fun SimpleSyntax<SignUpScreenState, SignUpSideEffect>.validateInput(): Boolean {
        var isValid = true

        if (state.email.isBlank()) {
            reduce { state.copy(emailError = UiText.StringResource(R.string.email_is_empty)) }
            isValid = false
        } else if (!EMAIL_REGEX.matches(state.email)) {
            reduce { state.copy(emailError = UiText.StringResource(R.string.invalid_email_format)) }
            isValid = false
        } else if (state.isEmailAvailable != true) {
            postSideEffect(SignUpSideEffect.ShowMessage(UiText.StringResource(R.string.email_check_required)))
            isValid = false
        }

        if (state.password.isBlank()) {
            reduce { state.copy(passwordError = UiText.StringResource(R.string.password_is_empty)) }
            isValid = false
        } else if (!PASSWORD_REGEX.matches(state.password)) {
            reduce { state.copy(passwordError = UiText.StringResource(R.string.invalid_password_format)) }
            isValid = false
        }

        if (state.confirmPassword.isBlank()) {
            reduce { state.copy(confirmPasswordError = UiText.StringResource(R.string.confirm_password_is_empty)) }
            isValid = false
        } else if (state.password != state.confirmPassword) {
            reduce { state.copy(confirmPasswordError = UiText.StringResource(R.string.password_not_match)) }
            isValid = false
        }

        if (state.name.isBlank()) {
            reduce { state.copy(nameError = UiText.StringResource(R.string.name_is_empty)) }
            isValid = false
        }

        if (state.gender == Gender.NONE) {
            postSideEffect(SignUpSideEffect.ShowMessage(UiText.StringResource(R.string.gender_is_not_selected)))
            isValid = false
        }

        if (state.address.isBlank()) {
            reduce { state.copy(addressError = UiText.StringResource(R.string.address_is_empty)) }
            isValid = false
        }

        if (state.phoneNumber.isBlank()) {
            reduce { state.copy(phoneNumberError = UiText.StringResource(R.string.phone_number_is_empty)) }
            isValid = false
        } else if (!PHONE_REGEX.matches(state.phoneNumber)) {
            reduce { state.copy(phoneNumberError = UiText.StringResource(R.string.invalid_phone_format)) }
            isValid = false
        }

        return isValid
    }
}
