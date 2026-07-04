package kr.co.presentation.feature.account.screen.signup

import app.cash.turbine.test
import androidx.lifecycle.SavedStateHandle
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kr.co.core.common.error.DomainError
import kr.co.core.common.logging.AppLogger
import kr.co.core.common.model.Gender
import kr.co.domain.feature.account.model.SignUpInfo
import kr.co.domain.feature.account.usecase.CheckEmailAvailabilityUseCase
import kr.co.domain.feature.account.usecase.CreateAccountUseCase
import kr.co.presentation.R
import kr.co.presentation.testing.BaseViewModelTest
import kr.co.presentation.testing.PresentationFixtures
import kr.co.presentation.testing.assertStringResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest : BaseViewModelTest() {
    private val logger = mockk<AppLogger>(relaxed = true)
    private val createAccount = mockk<CreateAccountUseCase>()
    private val checkEmailAvailability = mockk<CheckEmailAvailabilityUseCase>()

    @Test
    fun `email check success marks email as available`() = runPresentationTest {
        coEvery { checkEmailAvailability(PresentationFixtures.EMAIL) } returns Ok(true)
        val viewModel = createViewModel()
        viewModel.joinOrbitIntents()

        viewModel.handleAction(SignUpAction.EmailChanged(PresentationFixtures.EMAIL))
        viewModel.awaitState { it.email == PresentationFixtures.EMAIL }
        viewModel.handleAction(SignUpAction.EmailCheckClicked)
        viewModel.joinOrbitIntents()

        val state = viewModel.awaitState { it.isEmailAvailable == true && !it.isCheckingEmail }
        assertEquals(false, state.isCheckingEmail)
    }

    @Test
    fun `invalid email check sets email format error and skips usecase`() = runPresentationTest {
        val viewModel = createViewModel()
        viewModel.joinOrbitIntents()

        viewModel.handleAction(SignUpAction.EmailChanged("invalid-email"))
        viewModel.awaitState { it.email == "invalid-email" }
        viewModel.handleAction(SignUpAction.EmailCheckClicked)

        val state = viewModel.awaitState { it.emailError != null }
        assertStringResource(R.string.invalid_email_format, state.emailError!!)
        coVerify(exactly = 0) { checkEmailAvailability(any()) }
    }

    @Test
    fun `sign up click with missing fields sets validation errors`() = runPresentationTest {
        val viewModel = createViewModel()
        viewModel.joinOrbitIntents()

        viewModel.handleAction(SignUpAction.SignUpClicked)
        viewModel.joinOrbitIntents()

        val state = viewModel.awaitState {
            it.emailError != null &&
                it.passwordError != null &&
                it.confirmPasswordError != null &&
                it.nameError != null &&
                it.addressError != null &&
                it.phoneNumberError != null
        }
        assertStringResource(R.string.email_is_empty, state.emailError!!)
        assertStringResource(R.string.password_is_empty, state.passwordError!!)
        assertStringResource(R.string.confirm_password_is_empty, state.confirmPasswordError!!)
        assertStringResource(R.string.name_is_empty, state.nameError!!)
        assertStringResource(R.string.address_is_empty, state.addressError!!)
        assertStringResource(R.string.phone_number_is_empty, state.phoneNumberError!!)
        coVerify(exactly = 0) { createAccount(any()) }
    }

    @Test
    fun `sign up success creates account and emits succeeded side effect`() = runPresentationTest {
        coEvery { checkEmailAvailability(PresentationFixtures.EMAIL) } returns Ok(true)
        val signUpInfoSlot = slot<SignUpInfo>()
        coEvery { createAccount(capture(signUpInfoSlot)) } returns Ok(Unit)
        val viewModel = createViewModel()
        viewModel.joinOrbitIntents()
        fillValidForm(viewModel)
        viewModel.joinOrbitIntents()
        viewModel.awaitState { it.hasValidFormValues() }
        viewModel.handleAction(SignUpAction.EmailCheckClicked)
        viewModel.joinOrbitIntents()
        viewModel.awaitState { it.isEmailAvailable == true && !it.isCheckingEmail }

        viewModel.sideEffectFlow().test {
            viewModel.handleAction(SignUpAction.SignUpClicked)
            viewModel.joinOrbitIntents()

            assertEquals(SignUpSideEffect.SignUpSucceeded, awaitItem())
            assertTrue(signUpInfoSlot.isCaptured)
            assertEquals(PresentationFixtures.EMAIL, signUpInfoSlot.captured.email)
            assertEquals("Password1", signUpInfoSlot.captured.password)
            assertEquals(PresentationFixtures.NAME, signUpInfoSlot.captured.name)
            assertEquals(Gender.FEMALE, signUpInfoSlot.captured.gender)
        }
    }

    @Test
    fun `sign up failure maps network error to message`() = runPresentationTest {
        coEvery { checkEmailAvailability(PresentationFixtures.EMAIL) } returns Ok(true)
        coEvery { createAccount(any()) } returns Err(DomainError.NetworkUnavailable)
        val viewModel = createViewModel()
        viewModel.joinOrbitIntents()
        fillValidForm(viewModel)
        viewModel.joinOrbitIntents()
        viewModel.awaitState { it.hasValidFormValues() }
        viewModel.handleAction(SignUpAction.EmailCheckClicked)
        viewModel.joinOrbitIntents()
        viewModel.awaitState { it.isEmailAvailable == true && !it.isCheckingEmail }

        viewModel.sideEffectFlow().test {
            viewModel.handleAction(SignUpAction.SignUpClicked)
            viewModel.joinOrbitIntents()

            val sideEffect = awaitItem() as SignUpSideEffect.ShowMessage
            assertStringResource(R.string.network_unavailable, sideEffect.uiText)
        }
    }

    private fun fillValidForm(viewModel: SignUpViewModel) {
        viewModel.handleAction(SignUpAction.EmailChanged(PresentationFixtures.EMAIL))
        viewModel.handleAction(SignUpAction.PasswordChanged("Password1"))
        viewModel.handleAction(SignUpAction.ConfirmPasswordChanged("Password1"))
        viewModel.handleAction(SignUpAction.NameChanged(PresentationFixtures.NAME))
        viewModel.handleAction(SignUpAction.GenderChanged(Gender.FEMALE))
        viewModel.handleAction(SignUpAction.BirthdayChanged(PresentationFixtures.DATE))
        viewModel.handleAction(SignUpAction.AddressChanged(PresentationFixtures.ADDRESS))
        viewModel.handleAction(SignUpAction.PhoneNumberChanged(PresentationFixtures.PHONE_NUMBER))
    }

    private fun SignUpScreenState.hasValidFormValues() =
        email == PresentationFixtures.EMAIL &&
            password == "Password1" &&
            confirmPassword == "Password1" &&
            name == PresentationFixtures.NAME &&
            gender == Gender.FEMALE &&
            birthday == PresentationFixtures.DATE &&
            address == PresentationFixtures.ADDRESS &&
            phoneNumber == PresentationFixtures.PHONE_NUMBER

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = SignUpViewModel(
        logger = logger,
        savedStateHandle = savedStateHandle,
        createAccount = createAccount,
        checkEmailAvailabilityUseCase = checkEmailAvailability,
    ).trackOrbitViewModel()
}
