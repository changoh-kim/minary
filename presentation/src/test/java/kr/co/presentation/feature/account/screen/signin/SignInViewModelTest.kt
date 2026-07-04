package kr.co.presentation.feature.account.screen.signin

import app.cash.turbine.test
import androidx.lifecycle.SavedStateHandle
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kr.co.core.common.error.DomainError
import kr.co.core.common.logging.AppLogger
import kr.co.domain.feature.account.usecase.SignInUseCase
import kr.co.presentation.R
import kr.co.presentation.testing.BaseViewModelTest
import kr.co.presentation.testing.PresentationFixtures
import kr.co.presentation.testing.assertStringResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest : BaseViewModelTest() {
    private val logger = mockk<AppLogger>(relaxed = true)
    private val signIn = mockk<SignInUseCase>()

    @Test
    fun `init restores saved credentials`() = runPresentationTest {
        val viewModel = createViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "email" to "saved@example.test",
                    "password" to "password-test",
                )
            )
        )
        viewModel.joinOrbitIntents()

        val state = viewModel.awaitState { it.email == "saved@example.test" }

        assertEquals("saved@example.test", state.email)
        assertEquals("password-test", state.password)
    }

    @Test
    fun `sign in click with blank email emits validation message and skips usecase`() = runPresentationTest {
        val viewModel = createViewModel()
        viewModel.joinOrbitIntents()

        viewModel.sideEffectFlow().test {
            viewModel.handleAction(SignInAction.SignInClicked)
            viewModel.joinOrbitIntents()

            val sideEffect = awaitItem() as SignInSideEffect.ShowMessage
            assertStringResource(R.string.id_is_empty, sideEffect.uiText)
            coVerify(exactly = 0) { signIn(any(), any()) }
        }
    }

    @Test
    fun `sign in success emits succeeded side effect`() = runPresentationTest {
        coEvery { signIn(PresentationFixtures.EMAIL, "Password1") } returns Ok(PresentationFixtures.account())
        val viewModel = createViewModel()
        viewModel.joinOrbitIntents()

        viewModel.handleAction(SignInAction.EmailChanged(PresentationFixtures.EMAIL))
        viewModel.handleAction(SignInAction.PasswordChanged("Password1"))
        viewModel.joinOrbitIntents()
        viewModel.awaitState { it.email == PresentationFixtures.EMAIL && it.password == "Password1" }

        viewModel.sideEffectFlow().test {
            viewModel.handleAction(SignInAction.SignInClicked)
            viewModel.joinOrbitIntents()

            assertEquals(SignInSideEffect.SignInSucceeded, awaitItem())
            assertEquals(false, viewModel.stateFlow().value.isSigningIn)
        }
    }

    @Test
    fun `sign in failure maps auth error to message`() = runPresentationTest {
        coEvery { signIn(PresentationFixtures.EMAIL, "Password1") } returns Err(DomainError.Auth.InvalidCredentials)
        val viewModel = createViewModel()
        viewModel.joinOrbitIntents()

        viewModel.handleAction(SignInAction.EmailChanged(PresentationFixtures.EMAIL))
        viewModel.handleAction(SignInAction.PasswordChanged("Password1"))
        viewModel.joinOrbitIntents()
        viewModel.awaitState { it.email == PresentationFixtures.EMAIL && it.password == "Password1" }

        viewModel.sideEffectFlow().test {
            viewModel.handleAction(SignInAction.SignInClicked)
            viewModel.joinOrbitIntents()

            val sideEffect = awaitItem() as SignInSideEffect.ShowMessage
            assertStringResource(R.string.invalid_credentials, sideEffect.uiText)
        }
    }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = SignInViewModel(
        logger = logger,
        savedStateHandle = savedStateHandle,
        signIn = signIn,
    ).trackOrbitViewModel()
}
